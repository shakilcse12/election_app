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

    // Fetch all or search. DAO handles the wildcard logic.
    fun getVoteCenters(query: String): Flow<List<VoteCenterEntity>> {
        return if (query.isBlank()) {
            dao.getAll()
        } else {
            // Your DAO query likely uses: LIKE '%' || :query || '%'
            dao.searchCenters(query)
        }
    }

    fun getAllCenters(): Flow<List<VoteCenterEntity>> = dao.getAll()

    suspend fun insertCenters(centers: List<VoteCenterEntity>) = dao.insertAll(centers)

    suspend fun clearAll() = dao.clearAll()

    // Returns nullable to be safe
    suspend fun getById(id: Int): VoteCenterEntity? = dao.getById(id)

    suspend fun save(center: VoteCenterEntity) = dao.insert(normalize(center))

    private fun normalize(center: VoteCenterEntity): VoteCenterEntity {
        return center.copy(
            centerName = center.centerName.trim().replace("\\s+".toRegex(), " "),
            presidingOfficerName = center.presidingOfficerName.trim(),
            address = center.address.trim()
        )
    }
}