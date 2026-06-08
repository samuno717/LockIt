package com.example.lockit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val email: String,
    val username: String,
    val password: String,
    val website: String,
    val category: String,
    val iconRes: Int = 0,
    // Drawable resource name of the chosen icon (e.g. "fi_brands_steam"); "" = letter avatar.
    val iconKey: String = ""
)
