package com.firestormsw.listflow.data.viewmodel

import com.firestormsw.listflow.data.repository.ListItemRepository
import com.firestormsw.listflow.data.repository.ListRepository
import com.firestormsw.listflow.data.repository.PeerRepository
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttManager @Inject constructor(
    private val listRepository: ListRepository,
    private val listItemRepository: ListItemRepository,
    private val peerRepository: PeerRepository,
) {
    private val mqttBrokerUrl = "tcp://127.0.0.1:1883"

    private var mqttClient: MqttClient? = null
    private val mqttConnectOptions = MqttConnectOptions().apply {
        isCleanSession = false
        isAutomaticReconnect = true
        connectionTimeout = 30
        keepAliveInterval = 60
        maxInflight = 100
    }
}