package com.firestormsw.listflow.data.networking

import android.util.Log
import com.firestormsw.listflow.TAG
import com.firestormsw.listflow.utils.CryptoUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.crypto.SecretKey

class MqttTopic<TMessage>(
    private val connectionManager: MqttConnectionManager,
    private val topicName: String,
    private val encryptionKey: SecretKey?,
    private val serializer: KSerializer<TMessage>,
) {
    private var isRetained: Boolean = false
    private var qos: Int = 1

    fun setIsRetained(isRetained: Boolean): MqttTopic<TMessage> {
        this.isRetained = isRetained
        return this
    }

    fun setQos(qos: Int): MqttTopic<TMessage> {
        this.qos = qos
        return this
    }

    fun publish(message: TMessage) {
        val jsonString = Json.encodeToString(serializer, message)
        var messageBytes = jsonString.toByteArray(Charsets.UTF_8)
        if (encryptionKey != null) {
            messageBytes = CryptoUtils.encryptData(messageBytes, encryptionKey)
        }

        val mqttMessage = MqttMessage(messageBytes)
        mqttMessage.isRetained = isRetained
        mqttMessage.qos = qos

        connectionManager.getClient().publish(topicName, mqttMessage)
    }

    fun subscribe(callback: (TMessage) -> Unit) {
        connectionManager.getClient().subscribe(topicName) { _, message ->
            var plaintextMessage = message.payload
            try {
                if (encryptionKey != null) {
                    plaintextMessage = CryptoUtils.decryptData(plaintextMessage, encryptionKey)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt MQTT message received on topic '$topicName'", e)
            }

            try {
                val jsonString = plaintextMessage.decodeToString()
                val messageObject = Json.decodeFromString(serializer, jsonString)
                callback(messageObject)
            } catch (e: Exception) {
                Log.e(TAG, "An error has occurred while processing an incoming MQTT message on topic '$topicName'", e)
            }
        }
    }

    companion object {
        inline fun <reified T> create(
            connectionManager: MqttConnectionManager,
            topicName: String,
            encryptionKey: SecretKey? = null,
        ): MqttTopic<T> {
            return MqttTopic(connectionManager, topicName, encryptionKey, serializer())
        }
    }
}