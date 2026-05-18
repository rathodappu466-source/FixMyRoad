package com.example.fixmyroad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fixmyroad.data.local.entity.ReportEntity

@Database(entities = [ReportEntity::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}
