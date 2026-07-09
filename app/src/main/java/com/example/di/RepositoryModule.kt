package com.example.di

import com.example.data.repository.FocusRepositoryImpl
import com.example.domain.repository.FocusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFocusRepository(
        focusRepositoryImpl: FocusRepositoryImpl
    ): FocusRepository

    @Binds
    @Singleton
    abstract fun bindTimetableRepository(
        timetableRepositoryImpl: com.example.data.repository.TimetableRepositoryImpl
    ): com.example.domain.repository.TimetableRepository

    @Binds
    @Singleton
    abstract fun bindBlockerRepository(
        blockerRepositoryImpl: com.example.data.repository.BlockerRepositoryImpl
    ): com.example.domain.repository.BlockerRepository

    @Binds
    @Singleton
    abstract fun bindSpeechRepository(
        speechRepositoryImpl: com.example.data.repository.SpeechRepositoryImpl
    ): com.example.domain.repository.SpeechRepository

    @Binds
    @Singleton
    abstract fun bindSecurityRepository(
        securityRepositoryImpl: com.example.data.repository.SecurityRepositoryImpl
    ): com.example.domain.repository.SecurityRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: com.example.data.repository.AnalyticsRepositoryImpl
    ): com.example.domain.repository.AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: com.example.data.repository.BackupRepositoryImpl
    ): com.example.domain.repository.BackupRepository
}
