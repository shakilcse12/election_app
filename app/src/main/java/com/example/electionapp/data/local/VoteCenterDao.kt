package com.example.electionapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteCenterDao {

    @Query("SELECT * FROM vote_centers ORDER BY centerNumber")
    fun getAllCenters(): Flow<List<VoteCenterEntity>>

    @Query("SELECT * FROM vote_centers WHERE id = :id")
    suspend fun getCenterById(id: Int): VoteCenterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(centers: List<VoteCenterEntity>)
}
