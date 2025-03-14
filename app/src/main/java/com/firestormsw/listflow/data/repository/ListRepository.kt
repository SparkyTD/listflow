package com.firestormsw.listflow.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.firestormsw.listflow.data.dao.ListDao
import com.firestormsw.listflow.data.model.ListModel
import javax.inject.Inject

class ListRepository @Inject constructor(
    private val listDao: ListDao
) : RepositoryBase() {
    fun getListsWithItems(): LiveData<List<ListModel>> {
        return listDao.getListsWithItems().map {
            it.map { list ->
                list.list.toModel().copy(items = list.items.map { item -> item.toModel() })
            }
        }
    }

    fun getListWithItems(listId: String): ListModel {
        val listWithItems = listDao.getListWithItems(listId)
        return ListModel(
            id = listWithItems.list.id,
            name = listWithItems.list.name,
            isCheckedExpanded = listWithItems.list.isCheckedHidden,
            items = listWithItems.items.map { it.toModel() }
        )
    }

    fun upsertList(list: ListModel) {
        listDao.upsertList(list.toEntity())
    }

    fun deleteList(list: ListModel) {
        listDao.deleteList(list.toEntity())
    }

    fun uncheckAllInList(listId: String) {
        listDao.uncheckAllInList(listId)
    }
}