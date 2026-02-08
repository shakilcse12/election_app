package com.example.electionapp.data.repository

import android.content.Context
import android.util.Log
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterDto
import com.example.electionapp.data.local.entity.VoteCenterEntity
import com.example.electionapp.util.normalizeBanglaSafe
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
        val trimmed = query.trim().normalizeBanglaSafe() // Add this line
        return if (trimmed.isBlank()) {
            dao.getAll()
        } else {
            val searchTerm = "%$trimmed%"
            Log.d("REPOSITORY_DEBUG", "Search term: '$searchTerm'")

            dao.searchCenters(searchTerm).map { centers ->
                Log.d("REPOSITORY_DEBUG", "Found ${centers.size} centers for query '$trimmed'")

                // Filter the results to only include centers that actually match the query
                val filteredCenters = centers.filter { center ->
                    // 1. Extract the displayable name once per item
                    val searchableOfficerName = center.presidingOfficerName
                        .split(Regex("[,|৷]"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString(", ")

                    // 2. Check all relevant fields
                    val matches = center.centerName.contains(trimmed) ||
                            searchableOfficerName.contains(trimmed) ||
                            center.address.contains(trimmed) ||
                            center.centerNumber.toString().contains(trimmed)

                    //matches
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
            centerName = center.centerName
                .trim()
                .normalizeBanglaSafe(),

            presidingOfficerName = center.presidingOfficerName
                .trim()
                .normalizeBanglaSafe(),

            address = center.address
                .trim()
                .normalizeBanglaSafe(),

            union = center.union
                .trim()
                .normalizeBanglaSafe()

        )
    }

    suspend fun seedVoteCentersIfNeeded() {
        val prefs = context.getSharedPreferences("seed_prefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("vote_centers_seeded", false)) return

        try {
            val json = context.assets
                .open("vote_centers.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val type = object : TypeToken<List<VoteCenterDto>>() {}.type
            val dtoList: List<VoteCenterDto> = Gson().fromJson(json, type)

            // ✅ FIX: Normalize the entities BEFORE inserting them into the database
            val normalizedEntities = dtoList.map { dto ->
                normalize(dto.toEntity())
            }

            dao.insertAll(normalizedEntities)

            prefs.edit().putBoolean("vote_centers_seeded", true).apply()
        } catch (e: Exception) {
            Log.e("REPO", "Seed failed", e)
        }
    }
}