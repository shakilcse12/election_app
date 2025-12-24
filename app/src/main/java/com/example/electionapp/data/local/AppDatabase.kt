package com.example.electionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VoteCenterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voteCenterDao(): VoteCenterDao
}
