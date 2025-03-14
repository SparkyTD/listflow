package com.firestormsw.listflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.firestormsw.listflow.data.entity.ListEntity
import com.firestormsw.listflow.data.entity.ListWithItems

@Dao
interface ListDao {
    @Upsert
    fun upsertList(list: ListEntity)

    @Delete
    fun deleteList(list: ListEntity)

    @Query("SELECT * FROM lists ORDER BY id DESC")
    fun getLists(): LiveData<List<ListEntity>>

    @Transaction
    @Query("SELECT * FROM lists ORDER BY id DESC")
    fun getListsWithItems(): LiveData<List<ListWithItems>>

    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    fun getListWithItems(listId: String): ListWithItems

    @Query("SELECT * FROM lists WHERE id = :listId")
    fun getList(listId: String): ListEntity

    @Query("UPDATE items SET isChecked = 0 WHERE listId = :listId")
    fun uncheckAllInList(listId: String)
}