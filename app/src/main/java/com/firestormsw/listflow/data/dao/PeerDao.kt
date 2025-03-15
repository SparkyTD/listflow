package com.firestormsw.listflow.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.firestormsw.listflow.data.entity.ListEntity
import com.firestormsw.listflow.data.entity.PeerEntity

@Dao
interface PeerDao {
    @Upsert
    fun upsertPeer(peer: PeerEntity)

    @Delete
    fun deletePeer(peer: PeerEntity)

    @Query("SELECT * FROM peers WHERE listId = :listId")
    fun getPeersForList(listId: String): List<PeerEntity>

    @Query("SELECT DISTINCT lists.* FROM lists INNER JOIN peers ON lists.id = peers.listId")
    fun getListsWithPeers(): List<ListEntity>

    @Query("SELECT count(*) != 0 FROM peers WHERE listId = :listId")
    fun getIsListShared(listId: String): LiveData<Boolean>
}