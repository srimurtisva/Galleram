package com.srimurtiseva.galleram.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.srimurtiseva.galleram.data.local.dao.MediaDao
import com.srimurtiseva.galleram.data.local.entities.MediaEntity

@Database(entities = [MediaEntity::class], version = 1, exportSchema = false)
abstract class GalleramDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: GalleramDatabase? = null

        fun getDatabase(context: Context): GalleramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GalleramDatabase::class.java,
                    "galleram_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
