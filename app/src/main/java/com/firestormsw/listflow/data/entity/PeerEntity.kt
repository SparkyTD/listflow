package com.firestormsw.listflow.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "peers",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class PeerEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val peerDeviceId: String?,
    val localDeviceId: String,
    val sharedAesKey: String?,
)