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
    val iconKey: String = ""
)
