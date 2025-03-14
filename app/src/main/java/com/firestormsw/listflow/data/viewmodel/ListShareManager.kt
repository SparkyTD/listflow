package com.firestormsw.listflow.data.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.firestormsw.listflow.TAG
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.model.PeerModel
import com.firestormsw.listflow.data.repository.ListItemRepository
import com.firestormsw.listflow.data.repository.ListRepository
import com.firestormsw.listflow.data.repository.PeerRepository
import com.firestormsw.listflow.utils.CryptoUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeHex
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import ulid.ULID
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
@Singleton
@SuppressLint("HardwareIds")
class ListShareManager @Inject constructor(
    private val listRepository: ListRepository,
    private val listItemRepository: ListItemRepository,
    private val peerRepository: PeerRepository,
    @ApplicationContext context: Context
) {
    private val mqttBrokerUrl = "tcp://192.168.1.108:1883"
    private var mqttClient: MqttClient? = null
    private val mqttConnectOptions = MqttConnectOptions().apply {
        isCleanSession = false
        isAutomaticReconnect = true
        connectionTimeout = 30
        keepAliveInterval = 60
        maxInflight = 100
    }

    private var deviceId: String = ULID.randomULID()

    init {
        val contentResolver = context.contentResolver
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    suspend fun connectIfHasPeers(force: Boolean = false): Boolean {
        if (!force && !peerRepository.getListsWithPeers().any()) {
            return false
        }

        if (mqttClient != null) {
            return true
        }

        try {
            val persistence: MqttClientPersistence = MemoryPersistence()
            mqttClient = MqttClient(
                mqttBrokerUrl,
                "listflow_$deviceId",
                persistence
            )

            while (true) {
                try {
                    mqttClient!!.connect(mqttConnectOptions)
                    Log.i(TAG, "MQTT connected successfully")
                    break
                } catch (e: MqttException) {
                    Log.e(TAG, "Failed to connect to the MQTT broker at '$mqttBrokerUrl', reattempting in 5 seconds")
                    delay(5000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to the MQTT broker: $e")
        }

        return true
    }

    fun setupPeerListeners() {
        peerRepository.getListsWithPeers().forEach { list ->
            val peers = peerRepository.getPeersForList(list.id)
            peers.forEach { peer ->
                startListeningForPeerChanges(peer)
            }
        }
    }

    suspend fun disconnect() {
        if (mqttClient == null) {
            return
        }

        delay(0)

        mqttClient!!.disconnect()
        mqttClient = null
    }

    suspend fun generateShareCode(list: ListModel, onCodeReady: (String) -> Unit) {
        val sessionId = ULID.randomULID()
        val keyPair = CryptoUtils.generateECDHKeyPair()
        val qrData = "$sessionId:${keyPair.public.encoded.toHexString(HexFormat.UpperCase)}"
        onCodeReady(qrData)

        // Ensure connection
        connectIfHasPeers(true)

        // Subscribe to handshake topic
        mqttClient!!.subscribe("lf_pair_$sessionId") { _, message ->
            val messageParts = message.toString().split(":")
            val peerDeviceId = messageParts[0]
            val publicKeyString = messageParts[1]
            val peerPublicKey = CryptoUtils.decodePublicKey(publicKeyString)

            val sharedSecret = CryptoUtils.deriveSharedSecret(keyPair.private, peerPublicKey)
            val aesKey = CryptoUtils.deriveAESKey(sharedSecret)
            val peerInformation = PeerModel(
                id = sessionId,
                listId = list.id,
                peerDeviceId = peerDeviceId,
                localDeviceId = deviceId,
                sharedAesKey = aesKey.encoded.toHexString(),
            )

            peerRepository.upsertPeer(peerInformation)

            // Send our device ID back, along with the full list information
            val initialData = Json.encodeToString(ListModel.serializer(), list)
            val initMessage = "$deviceId:${list.id}:${Base64.encode(initialData.toByteArray(Charsets.UTF_8))}"
            val encInitMessage = CryptoUtils.encryptData(initMessage.toByteArray(), aesKey)

            mqttClient!!.publish("lf_init_$sessionId", MqttMessage(encInitMessage))

            startListeningForPeerChanges(peerInformation)
        }
    }

    private fun startListeningForPeerChanges(peer: PeerModel) {
        mqttClient!!.subscribe("lf_update_${peer.id}") { _, encryptedMessage ->
            val secretKey = SecretKeySpec(peer.sharedAesKey!!.decodeHex().toByteArray(), "AES")
            val messageBytes = CryptoUtils.decryptData(encryptedMessage.payload, secretKey)
            val message = messageBytes.decodeToString()
            val remoteList = Json.decodeFromString<ListModel>(message)

            val localList = listRepository.getListWithItems(remoteList.id)

            for (remoteItem in remoteList.items) {
                listItemRepository.upsertListItem(remoteItem)
            }

            for (localItem in localList.items) {
                val remoteItem = remoteList.items.firstOrNull { it.id == localItem.id }
                if (remoteItem == null) {
                    listItemRepository.deleteListItem(localItem)
                }
            }

            Log.w(TAG, "MQTT UPDATE >> $remoteList")
        }
    }

    suspend fun processScannedShareCode(code: String) {
        val codeParts = code.split(':')
        if (codeParts.size != 2 || codeParts[0].length != 26) {
            Log.e(TAG, "Invalid QR code scanned, ignoring")
            return
        }

        val sessionId = codeParts[0]
        val peerPublicKey = CryptoUtils.decodePublicKey(codeParts[1])

        // Generate keypair for this device
        val keyPair = CryptoUtils.generateECDHKeyPair()
        val sharedSecret = CryptoUtils.deriveSharedSecret(keyPair.private, peerPublicKey)
        val aesKey = CryptoUtils.deriveAESKey(sharedSecret)

        // Ensure connection
        connectIfHasPeers(true)

        mqttClient!!.subscribe("lf_init_$sessionId") { _, encryptedMessage ->
            val message = CryptoUtils.decryptData(encryptedMessage.payload, aesKey)
            val messageParts = message.decodeToString().split(':')
            val peerDeviceId = messageParts[0]
            val listId = messageParts[1]
            val initData = Base64.decode(messageParts[2]).decodeToString()
            val peerInformation = PeerModel(
                id = sessionId,
                listId = listId,
                peerDeviceId = peerDeviceId,
                localDeviceId = deviceId,
                sharedAesKey = aesKey.encoded.toHexString(),
            )
            val list = Json.decodeFromString<ListModel>(initData)
            Log.w(TAG, "SYNC INIT_DATA: $initData")
            Log.w(TAG, "Attempting to insert: $peerInformation")

            // Initial list creation
            listRepository.upsertList(list)
            list.items.forEach { listItemRepository.upsertListItem(it) }
            peerRepository.upsertPeer(peerInformation)
            // todo set selected list

            startListeningForPeerChanges(peerInformation)
        }

        val message = "$deviceId:${keyPair.public.encoded.toHexString(HexFormat.UpperCase)}"
        mqttClient!!.publish("lf_pair_$sessionId", MqttMessage(message.toByteArray(Charsets.UTF_8)))
    }

    suspend fun handleLocalListModified(listId: String) {
        peerRepository.getPeersForList(listId).forEach { peer ->
            val list = listRepository.getListWithItems(peer.listId)

            connectIfHasPeers(true)

            val listData = Json.encodeToString(ListModel.serializer(), list)
            val secretKey = SecretKeySpec(peer.sharedAesKey!!.decodeHex().toByteArray(), "AES")
            val messageBytes = listData.toByteArray(Charsets.UTF_8)
            val encryptedBytes = CryptoUtils.encryptData(messageBytes, secretKey)
            val message = MqttMessage(encryptedBytes)
            message.isRetained = true

            mqttClient!!.publish("lf_update_${peer.id}", message)
        }
    }
}