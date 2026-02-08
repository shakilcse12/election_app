package com.example.electionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.electionapp.data.local.entity.VoteCenterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteCenterDao {

    @Query("SELECT * FROM vote_centers ORDER BY centerNumber ASC")
    fun getAllCenters(): Flow<List<VoteCenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(centers: List<VoteCenterEntity>)

    @Query("DELETE FROM vote_centers")
    suspend fun clearAll()

    @Query("SELECT * FROM vote_centers WHERE id = :id")
    suspend fun getById(id: Int): VoteCenterEntity?

    /**
     * Search query covering:
     * 1. Center Name
     * 2. Presiding Officer Name
     * 3. Address
     * 4. Center Number (via CAST)
     * 5. Presiding Officer Phone (Added for convenience)
     */

    @Query("""
    SELECT * FROM vote_centers 
    WHERE 
        centerName LIKE :searchTerm 
        OR presidingOfficerName LIKE :searchTerm 
        OR address LIKE :searchTerm
        OR CAST(centerNumber AS TEXT) LIKE :searchTerm
    ORDER BY centerNumber ASC
""")
    fun searchCenters(searchTerm: String): Flow<List<VoteCenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(center: VoteCenterEntity)

    @Query("SELECT * FROM vote_centers ORDER BY centerNumber ASC")
    fun getAll(): Flow<List<VoteCenterEntity>>

    @Transaction
    suspend fun clearAndInsert(centers: List<VoteCenterEntity>) {
        clearAll()
        insertAll(centers)
    }
}
