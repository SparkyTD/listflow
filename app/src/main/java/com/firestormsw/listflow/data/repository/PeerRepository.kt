package com.firestormsw.listflow.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.firestormsw.listflow.data.dao.PeerDao
import com.firestormsw.listflow.data.model.ListModel
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

    fun getPeersForList(list: ListModel): LiveData<List<PeerModel>> {
        return peerDao.getPeersForList(list.id).map {
            it.map { entity -> entity.toModel() }
        }
    }
}