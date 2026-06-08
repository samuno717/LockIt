package com.example.lockit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lockit.model.Category
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User

@Database(entities = [User::class, PasswordEntry::class, Category::class], version = 3, exportSchema = false)
abstract class LockItDatabase : RoomDatabase() {
    abstract fun dao(): LockItDao

    companion object {
        @Volatile
        private var INSTANCE: LockItDatabase? = null

        private val DEFAULT_CATEGORIES = listOf("Vault", "Games", "Social Media", "Work")

        // v1 -> v2: introduce the categories table and seed the default folders.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name ON categories(name)"
                )
                DEFAULT_CATEGORIES.forEach { name ->
                    db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES (?)", arrayOf<Any>(name))
                }
            }
        }

        // v2 -> v3: add the per-entry icon key (drawable resource name).
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE password_entries ADD COLUMN iconKey TEXT NOT NULL DEFAULT ''")
            }
        }

        // Seed the default folders on a brand-new install (no migration runs).
        private val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DEFAULT_CATEGORIES.forEach { name ->
                    db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES (?)", arrayOf<Any>(name))
                }
            }
        }

        fun getDatabase(context: Context): LockItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LockItDatabase::class.java,
                    "lockit_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(SEED_CALLBACK)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
