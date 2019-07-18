package com.example.foodbuddy

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Message::class, UserStatus::class],
    exportSchema = false,
    version = 1
)
abstract class Database: RoomDatabase() {
    abstract fun messageDAO(): MessageDAO
    abstract fun userStatusDAO(): UserStatusDAO

    companion object {
        @Volatile private var instance: com.example.foodbuddy.Database? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            com.example.foodbuddy.Database::class.java, "test16.db")
            .build()
    }
}