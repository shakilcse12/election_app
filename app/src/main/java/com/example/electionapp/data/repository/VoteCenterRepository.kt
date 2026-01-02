package com.example.electionapp.data.repository

import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteCenterRepository @Inject constructor(
    private val dao: VoteCenterDao
) {

    // Fetch all centers or search by query
    fun getVoteCenters(query: String): Flow<List<VoteCenterEntity>> {
        return if (query.isBlank()) {
            dao.getAllCenters()
        } else {
            dao.searchCenters("%$query%")
        }
    }

    fun getAllCenters(): Flow<List<VoteCenterEntity>> = dao.getAll()

    // Insert dummy or new data
    suspend fun insertCenters(centers: List<VoteCenterEntity>) {
        dao.insertAll(centers)
    }

    // Clear all data
    suspend fun clearCenters() {
        dao.clearAll()
    }

    suspend fun insertAll(centers: List<VoteCenterEntity>) = dao.insertAll(centers)

    suspend fun clearAll() = dao.clearAll()

    suspend fun getCenter(id: Int) = dao.getById(id)
    suspend fun save(center: VoteCenterEntity) = dao.insert(center)
    suspend fun getById(id: Int): VoteCenterEntity = dao.getById(id)!!
}
