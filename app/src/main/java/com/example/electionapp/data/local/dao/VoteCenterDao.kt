package com.example.electionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    // FIXED: Added centerName and simplified to a single query call
    @Query("""
        SELECT * FROM vote_centers 
        WHERE centerName LIKE '%' || :query || '%' 
        OR presidingOfficerName LIKE '%' || :query || '%' 
        OR address LIKE '%' || :query || '%'
        OR CAST(centerNumber AS TEXT) LIKE '%' || :query || '%'
        ORDER BY centerNumber ASC
    """)
    fun searchCenters(query: String): Flow<List<VoteCenterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(center: VoteCenterEntity)

    @Query("SELECT * FROM vote_centers ORDER BY centerNumber ASC")
    fun getAll(): Flow<List<VoteCenterEntity>>
}
