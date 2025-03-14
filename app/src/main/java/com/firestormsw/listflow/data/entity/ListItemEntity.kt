package com.firestormsw.listflow.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "items",
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
data class ListItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val text: String,
    val quantity: Float?,
    val unit: String?,
    val isChecked: Boolean,
    val isHighlighted: Boolean,
    val createdAt: Date,
)
