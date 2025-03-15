package com.firestormsw.listflow.data.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.firestormsw.listflow.TAG
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.model.PeerModel
import com.firestormsw.listflow.data.networking.MqttConnectionManager
import com.firestormsw.listflow.data.repository.ListItemRepository
import com.firestormsw.listflow.data.repository.ListRepository
import com.firestormsw.listflow.data.repository.PeerRepository
import com.firestormsw.listflow.utils.CryptoUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import okio.ByteString.Companion.decodeHex
import ulid.ULID
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Suppress("SameParameterValue")
@OptIn(ExperimentalStdlibApi::class)
@Singleton
@SuppressLint("HardwareIds")
class ListShareManager @Inject constructor(
    private val listRepository: ListRepository,
    private val listItemRepository: ListItemRepository,
    private val peerRepository: PeerRepository,
    @ApplicationContext context: Context
) {
    private val connectionManager = MqttConnectionManager(
        //brokerUrl = "tcp://192.168.1.108:1883",
        brokerUrl = "tcp://mqtt.eclipseprojects.io:1883",
        clientId = "listflow_" + ULID.randomULID(),
    )

    init {
        val contentResolver = context.contentResolver
        connectionManager.setClientId(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
    }

    suspend fun connectIfHasPeers(force: Boolean = false): Boolean {
        if (!force && !peerRepository.getListsWithPeers().any()) {
            return false
        }

        connectionManager.connect()
        return true
    }

    private fun disconnectIfNoPeers() {
        if (peerRepository.getListsWithPeers().any()) {
            return
        }

        disconnect()
    }

    fun setupPeerListeners() {
        peerRepository.getListsWithPeers().forEach { list ->
            val peers = peerRepository.getPeersForList(list.id)
            peers.forEach { peer ->
                startListeningForPeerChanges(peer)
            }
        }
    }

    fun disconnect() {
        connectionManager.disconnect()
    }

    suspend fun generateShareCode(list: ListModel, onCodeReady: (String) -> Unit, onCodeScanned: () -> Unit) {
        // PROTO(0) - Generate a random session ID, encode it into a QR code on DeviceA
        val sessionId = ULID.randomULID()
        val keyPair = CryptoUtils.generateECDHKeyPair()
        val qrData = "$sessionId:${keyPair.public.encoded.toHexString(HexFormat.UpperCase)}"
        onCodeReady(qrData)

        // Ensure connection
        connectIfHasPeers(true)

        // Subscribe to handshake topic
        connectionManager
            .createPlaintextTopic<PairMessage>(mqttGetTopicName(MqttMessageType.Pair, sessionId))
            .subscribe { message ->
                val peerPublicKey = CryptoUtils.decodePublicKey(message.publicKeyHex)

                val sharedSecret = CryptoUtils.deriveSharedSecret(keyPair.private, peerPublicKey)
                val aesKey = CryptoUtils.deriveAESKey(sharedSecret)
                val peerInformation = PeerModel(
                    id = sessionId,
                    listId = list.id,
                    peerDeviceId = message.deviceId,
                    localDeviceId = connectionManager.getClientId(),
                    sharedAesKey = aesKey.encoded.toHexString(),
                )

                if (peerRepository.getPeersForList(list.id).any { peer -> peer.peerDeviceId == message.deviceId }) {
                    Log.d(TAG, "Will not accept Peer handshake because a share entry already exists")
                } else {
                    peerRepository.upsertPeer(peerInformation)
                    onCodeScanned()

                    // PROTO(2/e) - Send our device ID back, along with the full list information
                    connectionManager
                        .createEncryptedTopic<InitializeMessage>(mqttGetTopicName(MqttMessageType.Initialize, sessionId), aesKey)
                        .publish(
                            InitializeMessage(
                                deviceId = connectionManager.getClientId(),
                                listId = list.id,
                                initialList = list,
                            )
                        )

                    startListeningForPeerChanges(peerInformation)
                }
            }
    }

    private fun startListeningForPeerChanges(peer: PeerModel, onUpdateReceived: (() -> Unit)? = null) {
        val secretKey = SecretKeySpec(peer.sharedAesKey!!.decodeHex().toByteArray(), "AES")
        connectionManager
            .createEncryptedTopic<ListUpdateMessage>(mqttGetTopicName(MqttMessageType.Update, peer.id), secretKey)
            .subscribe { updateMessage ->
                if (updateMessage.updateList != null) {
                    val remoteList = updateMessage.updateList
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
                    if (onUpdateReceived != null) {
                        onUpdateReceived()
                        disconnectIfNoPeers()
                    }
                } else if (updateMessage.deleteListId != null) {
                    peerRepository.deletePeer(peer)
                }
            }
    }

    suspend fun processScannedShareCode(code: String, onListInitialized: ((ListModel) -> Unit)? = null) {
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

        connectionManager
            .createEncryptedTopic<InitializeMessage>(mqttGetTopicName(MqttMessageType.Initialize, sessionId), aesKey)
            .subscribe { message ->
                // PROTO(3/e) - Save the initial list info and peer information to the DB

                val peerInformation = PeerModel(
                    id = sessionId,
                    listId = message.listId,
                    peerDeviceId = message.deviceId,
                    localDeviceId = connectionManager.getClientId(),
                    sharedAesKey = aesKey.encoded.toHexString(),
                )

                if (peerRepository.getListsWithPeers().any { peerList -> peerList.id == message.listId }) {
                    Log.d(TAG, "This list is already shared, ignoring handshake")
                } else {
                    Log.w(TAG, "Attempting to insert: $peerInformation")
                    Log.w(TAG, "Saving shared list: ${message.initialList}")

                    // Initial list creation
                    listRepository.upsertList(message.initialList)
                    message.initialList.items.forEach { listItemRepository.upsertListItem(it) }
                    peerRepository.upsertPeer(peerInformation)

                    if (onListInitialized != null) {
                        onListInitialized(message.initialList)
                    }

                    startListeningForPeerChanges(peerInformation)
                }
            }

        // PROTO(1) - Send the generated keypair and ECDH public key back to DeviceB
        connectionManager
            .createPlaintextTopic<PairMessage>(mqttGetTopicName(MqttMessageType.Pair, sessionId)).publish(
                PairMessage(
                    deviceId = connectionManager.getClientId(),
                    publicKeyHex = keyPair.public.encoded.toHexString(HexFormat.UpperCase)
                )
            )
    }

    suspend fun handleLocalListModified(listId: String) {
        peerRepository.getPeersForList(listId).forEach { peer ->
            val list = listRepository.getListWithItems(peer.listId)
            connectIfHasPeers(true)

            val updateMessage = ListUpdateMessage(
                deleteListId = null,
                updateList = list,
            )

            val secretKey = SecretKeySpec(peer.sharedAesKey!!.decodeHex().toByteArray(), "AES")
            connectionManager
                .createEncryptedTopic<ListUpdateMessage>(mqttGetTopicName(MqttMessageType.Update, peer.id), secretKey)
                .setIsRetained(true)
                .publish(updateMessage)
        }
    }

    suspend fun handleLocalListDeleted(listId: String) {
        peerRepository.getPeersForList(listId).forEach { peer ->
            connectIfHasPeers(true)

            val updateMessage = ListUpdateMessage(
                deleteListId = listId,
                updateList = null,
            )

            val secretKey = SecretKeySpec(peer.sharedAesKey!!.decodeHex().toByteArray(), "AES")
            connectionManager
                .createEncryptedTopic<ListUpdateMessage>(mqttGetTopicName(MqttMessageType.Update, peer.id), secretKey)
                .setIsRetained(true)
                .publish(updateMessage)

            disconnectIfNoPeers()
        }
    }

    suspend fun synchronizeAllPeers() {
        val timeoutDuration = 30000L
        val allPeerJobs = mutableListOf<Deferred<Boolean>>()

        peerRepository.getListsWithPeers().forEach { list ->
            peerRepository.getPeersForList(list.id).forEach { peer ->
                val peerJob = CoroutineScope(Dispatchers.IO).async {
                    try {
                        withTimeout(timeoutDuration) {
                            suspendCancellableCoroutine { continuation ->
                                startListeningForPeerChanges(peer) {
                                    Log.d(TAG, "Received synchronization data from peer: $peer")
                                    if (continuation.isActive) {
                                        continuation.resume(true)
                                    }
                                }
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.d(TAG, "Timeout waiting for update from peer: $peer")
                        false
                    }
                }
                allPeerJobs.add(peerJob)
            }
        }

        allPeerJobs.awaitAll()

        Log.d(TAG, "All peers have been synchronized")
    }

    private fun mqttGetTopicName(type: MqttMessageType, topicSuffix: String): String {
        return buildString {
            append("lf_v2_")
            append(type.toString())
            append("_")
            append(topicSuffix)
        }
    }
}

enum class MqttMessageType {
    Pair,
    Initialize,
    Update,
}

@Serializable
data class ListUpdateMessage(
    val deleteListId: String?,
    val updateList: ListModel?,
)

@Serializable
data class PairMessage(
    val deviceId: String,
    val publicKeyHex: String,
)

@Serializable
data class InitializeMessage(
    val deviceId: String,
    val listId: String,
    val initialList: ListModel,
)