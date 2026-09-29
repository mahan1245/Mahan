package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.StudyDayEntity
import com.example.data.model.StudyItemEntity
import com.example.data.model.StudySlotEntity
import com.example.data.model.SubtaskEntity
import com.example.data.model.TestResultEntity

@Database(
    entities = [
        StudyDayEntity::class,
        StudyItemEntity::class,
        StudySlotEntity::class,
        SubtaskEntity::class,
        TestResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "konkuryar_study_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
