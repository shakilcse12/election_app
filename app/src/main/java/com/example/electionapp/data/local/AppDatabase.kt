package com.example.electionapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.entity.VoteCenterEntity

@Database(
    entities = [VoteCenterEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voteCenterDao(): VoteCenterDao
}
