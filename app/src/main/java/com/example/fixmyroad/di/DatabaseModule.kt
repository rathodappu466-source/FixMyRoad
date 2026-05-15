package com.example.fixmyroad.di

import android.content.Context
import androidx.room.Room
import com.example.fixmyroad.data.local.AppDatabase
import com.example.fixmyroad.data.local.ReportDao
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
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fixmyroad_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideReportDao(
        database: AppDatabase
    ): ReportDao {

        return database.reportDao()
    }
}