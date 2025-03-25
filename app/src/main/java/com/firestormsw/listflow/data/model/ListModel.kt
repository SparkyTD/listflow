package com.firestormsw.listflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ListModel(
    val id: String,
    val name: String,
    val isCheckedExpanded: Boolean,
    val items: List<ListItemModel>,
) {
    fun nameEquals(other: ListModel): Boolean {
        return getNormalizedName() == other.getNormalizedName()
    }

    private fun getNormalizedName(): String {
        return name.uppercase().replace("\\w".toRegex(), "")
    }
}