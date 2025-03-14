package com.firestormsw.listflow.data.repository

import com.firestormsw.listflow.data.entity.ListEntity
import com.firestormsw.listflow.data.entity.ListItemEntity
import com.firestormsw.listflow.data.entity.PeerEntity
import com.firestormsw.listflow.data.model.ListItemModel
import com.firestormsw.listflow.data.model.ListModel
import com.firestormsw.listflow.data.model.PeerModel

open class RepositoryBase {

    fun ListModel.toEntity(): ListEntity = ListEntity(
        id = id,
        name = name,
        isCheckedHidden = isCheckedExpanded,
    )

    fun ListEntity.toModel(): ListModel = ListModel(
        id = id,
        name = name,
        items = emptyList(),
        isCheckedExpanded = isCheckedHidden,
    )

    fun ListItemModel.toEntity(): ListItemEntity = ListItemEntity(
        id = id,
        text = text,
        quantity = quantity,
        unit = unit,
        isChecked = isChecked,
        isHighlighted = isHighlighted,
        createdAt = createdAt,
        listId = listId,
    )

    fun ListItemEntity.toModel(): ListItemModel = ListItemModel(
        id = id,
        text = text,
        quantity = quantity,
        unit = unit,
        isChecked = isChecked,
        isHighlighted = isHighlighted,
        createdAt = createdAt,
        listId = listId,
    )

    fun PeerModel.toEntity(): PeerEntity = PeerEntity(
        id = id,
        listId = listId,
        peerDeviceId = peerDeviceId,
        localDeviceId = localDeviceId,
        sharedAesKey = sharedAesKey,
    )

    fun PeerEntity.toModel(): PeerModel = PeerModel(
        id = id,
        listId = listId,
        peerDeviceId = peerDeviceId,
        localDeviceId = localDeviceId,
        sharedAesKey = sharedAesKey,
    )
}