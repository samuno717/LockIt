package com.example.lockit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User

@Database(entities = [User::class, PasswordEntry::class], version = 1, exportSchema = false)
abstract class LockItDatabase : RoomDatabase() {
    abstract fun dao(): LockItDao

    companion object {
        @Volatile
        private var INSTANCE: LockItDatabase? = null

        fun getDatabase(context: Context): LockItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LockItDatabase::class.java,
                    "lockit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
