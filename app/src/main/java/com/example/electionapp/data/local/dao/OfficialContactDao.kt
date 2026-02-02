package com.example.electionapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.*
import com.example.electionapp.data.local.entity.OfficialContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficialContactDao {

    @Query("""
        SELECT * FROM official_contacts
        WHERE isActive = 1
        ORDER BY department, displayOrder
    """)
    fun observeContacts(): Flow<List<OfficialContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: OfficialContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: OfficialContactEntity)

    @Update
    suspend fun update(contact: OfficialContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<OfficialContactEntity>)

    // ✅ Soft Delete: Sets isActive to 0 instead of removing the row
    @Query("UPDATE official_contacts SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Delete
    suspend fun delete(contact: OfficialContactEntity)
}

