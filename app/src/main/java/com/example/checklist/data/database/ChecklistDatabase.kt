package com.example.checklist.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.checklist.data.dao.ChecklistDao
import com.example.checklist.data.model.ProjectEntity
import com.example.checklist.data.model.StepEntity

@Database(
    entities = [ProjectEntity::class, StepEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChecklistDatabase : RoomDatabase() {
    // Exposes DAO to access database operations
    abstract fun projectDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: ChecklistDatabase? = null

        // Returns a singleton instance of the database
        fun getInstance(context: Context): ChecklistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChecklistDatabase::class.java,
                    "checklist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

