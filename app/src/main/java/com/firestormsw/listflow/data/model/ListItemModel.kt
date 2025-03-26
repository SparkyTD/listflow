package com.firestormsw.listflow.data.model

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.Date

@Serializable
data class ListItemModel(
    val id: String,
    val listId: String,
    val text: String,
    val quantity: Float?,
    val unit: String?,
    val isChecked: Boolean,
    val isHighlighted: Boolean,
    @kotlinx.serialization.Transient val createdAt: Date = Date.from(Instant.now()),
) {
    fun textEquals(other: ListItemModel): Boolean {
        return getNormalizedText() == other.getNormalizedText()
    }

    private fun getNormalizedText(): String {
        return text.uppercase().replace("\\s".toRegex(), "")
    }
}