package com.example.electionapp.data.repository

import android.content.Context
import android.util.Log
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterDto
import com.example.electionapp.data.local.entity.VoteCenterEntity
import com.example.electionapp.util.toEntity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteCenterRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
            address = center.address.trim(),
            union = center.union.trim()
        )
    }

    suspend fun seedVoteCentersIfNeeded() {
        val prefs = context.getSharedPreferences("seed_prefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("vote_centers_seeded", false)) return

        val json = context.assets
            .open("vote_centers.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<VoteCenterDto>>() {}.type
        val dtoList: List<VoteCenterDto> = Gson().fromJson(json, type)

        dao.insertAll(dtoList.map { it.toEntity() })

        prefs.edit().putBoolean("vote_centers_seeded", true).apply()
    }
}