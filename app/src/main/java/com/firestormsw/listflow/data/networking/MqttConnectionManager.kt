package com.firestormsw.listflow.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.firestormsw.listflow.TAG
import com.firestormsw.listflow.utils.CancellationToken
import kotlinx.coroutines.delay
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttClientPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.crypto.SecretKey

class MqttConnectionManager(
    private val brokerUrl: String,
    private var clientId: String,
    private val context: Context,
) {
    private var client: MqttClient? = null
    private val mqttConnectOptions = MqttConnectOptions().apply {
        isCleanSession = false
        isAutomaticReconnect = true
        connectionTimeout = 30
        keepAliveInterval = 60
        maxInflight = 100
    }

    suspend fun connect(cancellationToken: CancellationToken? = null): Boolean {
        if (client != null && client!!.isConnected) {
            return false
        }

        val persistence: MqttClientPersistence = MemoryPersistence()

        while (true) {
            if (cancellationToken?.isCancellationRequested() == true) {
                return false
            }

            if (!isOnline(context)) {
                Log.w(TAG, "No network connection, waiting for 10 seconds")
                delay(10000)
                continue
            }

            try {
                client = MqttClient(brokerUrl, clientId, persistence)
                client!!.connect(mqttConnectOptions)
                Log.i(TAG, "MQTT connected successfully")
                return true
            } catch (e: MqttException) {
                client = null
                Log.e(TAG, "Failed to connect to the MQTT broker at '$brokerUrl', reattempting in 5 seconds")
                delay(5000)
            }
        }
    }

    fun disconnect() {
        if (client == null) {
            return
        }

        client!!.disconnect()
        client = null
    }

    fun setClientId(id: String) {
        clientId = id
    }

    fun getClientId(): String {
        return clientId
    }

    inline fun <reified TMessage> createPlaintextTopic(name: String): MqttTopic<TMessage> {
        return MqttTopic.create<TMessage>(this, name)
    }

    inline fun <reified TMessage> createEncryptedTopic(name: String, key: SecretKey): MqttTopic<TMessage> {
        return MqttTopic.create<TMessage>(this, name, key)
    }

    fun getClient(): MqttClient {
        if (client == null) {
            throw Exception("An MQTT operation was attempted with no active client connection")
        }

        return client!!
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}