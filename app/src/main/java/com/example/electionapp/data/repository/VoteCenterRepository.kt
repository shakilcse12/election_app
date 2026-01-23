package com.example.electionapp.data.repository

import android.util.Log
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteCenterRepository @Inject constructor(
    private val dao: VoteCenterDao
) {

    /**
     * This method handles the logic for both list display and searching.
     * When the user types a number, it goes to searchCenters where
     * the DAO's CAST logic handles it.
     */
    fun getVoteCenters(query: String): Flow<List<VoteCenterEntity>> {
        val trimmed = query.trim() // Add this line
        return if (trimmed.isBlank()) {
            dao.getAll()
        } else {
            val searchTerm = "%$trimmed%"
            Log.d("REPOSITORY_DEBUG", "Search term: '$searchTerm'")

            dao.searchCenters(searchTerm).map { centers ->
                Log.d("REPOSITORY_DEBUG", "Found ${centers.size} centers for query '$trimmed'")

                // Filter the results to only include centers that actually match the query
                val filteredCenters = centers.filter { center ->
                    val matches = listOf(
                        center.centerName.contains(trimmed, ignoreCase = true),
                        center.presidingOfficerName.contains(trimmed, ignoreCase = true),
                        center.address.contains(trimmed, ignoreCase = true),
                        center.presidingOfficerPhone.contains(trimmed, ignoreCase = true),
                        center.centerNumber.toString().contains(trimmed)
                    ).any { it }

                    if (matches) {
                        Log.d("REPOSITORY_DEBUG",
                            "✓ Center #${center.centerNumber}: ${center.centerName} " +
                                    "(matches: centerName=${center.centerName.contains(trimmed, ignoreCase = true)}, " +
                                    "address=${center.address.contains(trimmed, ignoreCase = true)}, " +
                                    "phone=${center.presidingOfficerPhone.contains(trimmed, ignoreCase = true)}, " +
                                    "number=${center.centerNumber.toString().contains(trimmed)})"
                        )
                    } else {
                        Log.d("REPOSITORY_DEBUG",
                            "✗ Center #${center.centerNumber}: ${center.centerName} " +
                                    "(does NOT match '$trimmed')"
                        )
                    }

                    matches
                }

                Log.d("REPOSITORY_DEBUG", "After filtering: ${filteredCenters.size} centers")
                filteredCenters
            }
        }
    }

    fun getAllCenters(): Flow<List<VoteCenterEntity>> = dao.getAll()

    suspend fun insertCenters(centers: List<VoteCenterEntity>) = dao.insertAll(centers)

    suspend fun clearAll() = dao.clearAll()

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