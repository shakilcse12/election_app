package com.example.electionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Database(
    entities = [VoteCenterEntity::class],
    version = 3, // incremented for development
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voteCenterDao(): VoteCenterDao
}
