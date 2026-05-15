package com.example.fixmyroad.di

import com.example.fixmyroad.data.repository.ReportRepositoryImpl
import com.example.fixmyroad.data.repository.UserRepositoryImpl
import com.example.fixmyroad.domain.repository.ReportRepository
import com.example.fixmyroad.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}