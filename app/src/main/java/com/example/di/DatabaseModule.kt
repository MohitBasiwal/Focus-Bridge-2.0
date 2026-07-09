package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.database.dao.FocusDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Room-related singleton dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "focus_bridge_database"
        )
        .fallbackToDestructiveMigration() // Facilitates local schema changes seamlessly during prototyping
        .build()
    }

    @Provides
    @Singleton
    fun provideFocusDao(database: AppDatabase): FocusDao {
        return database.focusDao()
    }

    @Provides
    @Singleton
    fun provideStudySessionDao(database: AppDatabase): com.example.data.database.dao.StudySessionDao {
        return database.studySessionDao()
    }

    @Provides
    @Singleton
    fun provideBlockerDao(database: AppDatabase): com.example.data.database.dao.BlockerDao {
        return database.blockerDao()
    }

    @Provides
    @Singleton
    fun provideSpeechDao(database: AppDatabase): com.example.data.database.dao.SpeechDao {
        return database.speechDao()
    }

    @Provides
    @Singleton
    fun provideSecurityDao(database: AppDatabase): com.example.data.database.dao.SecurityDao {
        return database.securityDao()
    }

    @Provides
    @Singleton
    fun provideAnalyticsDao(database: AppDatabase): com.example.data.database.dao.AnalyticsDao {
        return database.analyticsDao()
    }
}
