package com.example.electionapp.di

import android.content.Context
import androidx.room.Room
import com.example.electionapp.data.local.AppDatabase
import com.example.electionapp.data.local.dao.VoteCenterDao
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
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "election_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVoteCenterDao(db: AppDatabase): VoteCenterDao {
        return db.voteCenterDao()
    }
}
