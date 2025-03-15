package com.firestormsw.listflow.data.networking

import android.util.Log
import com.firestormsw.listflow.TAG
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
) {
    private var client: MqttClient? = null
    private val mqttConnectOptions = MqttConnectOptions().apply {
        isCleanSession = false
        isAutomaticReconnect = true
        connectionTimeout = 30
        keepAliveInterval = 60
        maxInflight = 100
    }

    suspend fun connect() {
        if (client != null && client!!.isConnected) {
            return
        }

        val persistence: MqttClientPersistence = MemoryPersistence()
        client = MqttClient(brokerUrl, clientId, persistence)

        while (true) {
            try {
                client!!.connect(mqttConnectOptions)
                Log.i(TAG, "MQTT connected successfully")
                break
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
}