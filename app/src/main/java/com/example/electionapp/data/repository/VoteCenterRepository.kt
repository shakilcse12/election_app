package com.example.electionapp.data.repository

import com.example.electionapp.data.local.VoteCenterDao
import com.example.electionapp.data.local.VoteCenterEntity
import com.example.electionapp.util.DummyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class VoteCenterRepository(
    private val dao: VoteCenterDao
) {

    val centers: Flow<List<VoteCenterEntity>> = dao.getAllCenters()

    suspend fun getCenterById(id: Int): VoteCenterEntity? {
        return dao.getCenterById(id)
    }

    suspend fun insertDummyDataIfEmpty() {
        if (dao.getAllCenters().first().isEmpty()) {
            dao.insertAll(DummyData.voteCenters())
        }
    }
}
