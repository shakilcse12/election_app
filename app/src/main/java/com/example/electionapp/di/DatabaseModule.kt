package com.example.electionapp.di

import android.content.Context
import androidx.room.Room
import com.example.electionapp.data.local.AppDatabase
import com.example.electionapp.data.local.VoteCenterDao
import com.example.electionapp.data.repository.VoteCenterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        ).build()

    @Provides
    fun provideVoteCenterDao(db: AppDatabase): VoteCenterDao =
        db.voteCenterDao()

    @Provides
    fun provideRepository(
        dao: VoteCenterDao
    ): VoteCenterRepository =
        VoteCenterRepository(dao)
}
