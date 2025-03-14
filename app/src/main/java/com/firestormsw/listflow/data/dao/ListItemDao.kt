package com.firestormsw.listflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.firestormsw.listflow.data.entity.ListItemEntity

@Dao
interface ListItemDao {
    @Upsert
    fun upsertListItem(listItem: ListItemEntity)

    @Delete
    fun deleteListItem(listItem: ListItemEntity)

    @Query("SELECT * from items WHERE id = :listId")
    fun getListItems(listId: String): LiveData<List<ListItemEntity>>
}