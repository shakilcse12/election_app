package com.example.electionapp.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS official_contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                department TEXT NOT NULL,
                name TEXT NOT NULL,
                designation TEXT NOT NULL,
                phoneNumber TEXT NOT NULL,
                displayOrder INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
            """
        )
    }
}


