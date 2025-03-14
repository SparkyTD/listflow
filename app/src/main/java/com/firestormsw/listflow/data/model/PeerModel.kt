package com.firestormsw.listflow.data.model

data class PeerModel(
    val id: String,
    val listId: String,
    val peerDeviceId: String?,
    val localDeviceId: String,
    val sharedAesKey: String?,
)