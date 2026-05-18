package com.example.lockit.data.local

import androidx.room.*
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface LockItDao {
    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun getUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM password_entries")
    fun getAllPasswords(): Flow<List<PasswordEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(entry: PasswordEntry)

    @Delete
    suspend fun deletePassword(entry: PasswordEntry)

    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getPasswordById(id: Long): PasswordEntry?
    
    @Query("SELECT * FROM password_entries WHERE serviceName LIKE '%' || :query || '%'")
    fun searchPasswords(query: String): Flow<List<PasswordEntry>>
}
