package com.firestormsw.listflow.data.repository

import androidx.lifecycle.LiveData
import com.firestormsw.listflow.data.dao.PeerDao
import com.firestormsw.listflow.data.entity.ListEntity
import com.firestormsw.listflow.data.model.PeerModel
import javax.inject.Inject

class PeerRepository @Inject constructor(
    private val peerDao: PeerDao
) : RepositoryBase() {
    fun upsertPeer(peer: PeerModel) {
        peerDao.upsertPeer(peer.toEntity())
    }

    fun deletePeer(peer: PeerModel) {
        peerDao.deletePeer(peer.toEntity())
    }

    fun getPeersForList(listId: String): List<PeerModel> {
        return peerDao.getPeersForList(listId).map { it.toModel() }
    }

    fun getListsWithPeers(): List<ListEntity> {
        return peerDao.getListsWithPeers()
    }

    fun getIsListShared(listId: String): LiveData<Boolean> {
        return peerDao.getIsListShared(listId)
    }
}