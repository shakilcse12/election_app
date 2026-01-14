package com.example.electionapp.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
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
