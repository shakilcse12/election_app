package com.example.electionapp.di

import android.content.Context
import androidx.room.Room
import com.example.electionapp.data.local.AppDatabase
import com.example.electionapp.data.local.dao.VoteCenterDao
import com.example.electionapp.data.local.db.MIGRATION_3_4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "election_db"
        )
            .addMigrations(MIGRATION_3_4)
            .build()


    @Provides
    @Singleton
    fun provideVoteCenterDao(db: AppDatabase): VoteCenterDao {
        return db.voteCenterDao()
    }
}
