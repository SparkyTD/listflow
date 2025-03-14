package com.firestormsw.listflow.data.model

import java.util.Date

data class ListItemModel(
    val id: String,
    val listId: String,
    val text: String,
    val quantity: Float?,
    val unit: String?,
    val isChecked: Boolean,
    val isHighlighted: Boolean,
    val createdAt: Date,
)
