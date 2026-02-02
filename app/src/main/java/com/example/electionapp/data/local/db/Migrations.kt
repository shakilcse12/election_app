package com.example.electionapp.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_34 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {

        /*database.execSQL(
            """
            ALTER TABLE vote_centers 
            ADD COLUMN centerName TEXT NOT NULL DEFAULT ''
            """
        )

        database.execSQL(
            """
            ALTER TABLE vote_centers 
            ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0
            """
        )

        database.execSQL(
            """
            ALTER TABLE vote_centers 
            ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0
            """
        )*/
    }
}

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


