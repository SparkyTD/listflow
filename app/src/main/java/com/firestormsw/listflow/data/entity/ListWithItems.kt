package com.firestormsw.listflow.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ListWithItems(
    @Embedded val list: ListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<ListItemEntity>
)