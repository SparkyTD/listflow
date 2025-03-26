package com.firestormsw.listflow.data.repository

import com.firestormsw.listflow.data.dao.ListItemDao
import com.firestormsw.listflow.data.model.ListItemModel
import javax.inject.Inject

class ListItemRepository @Inject constructor(
    private val listItemDao: ListItemDao
) : RepositoryBase() {

    fun upsertListItem(item: ListItemModel) {
        val entity = item.toEntity()
        listItemDao.upsertListItem(entity)
    }

    fun deleteListItem(item: ListItemModel) {
        listItemDao.deleteListItem(item.toEntity())
    }
}