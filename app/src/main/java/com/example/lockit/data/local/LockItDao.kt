package com.example.lockit.data.local

import androidx.room.*
import com.example.lockit.model.Category
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

    @Query("SELECT * FROM password_entries")
    suspend fun getAllPasswordsOnce(): List<PasswordEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(entry: PasswordEntry)

    @Delete
    suspend fun deletePassword(entry: PasswordEntry)

    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getPasswordById(id: Long): PasswordEntry?
    
    @Query("SELECT * FROM password_entries WHERE serviceName LIKE '%' || :query || '%'")
    fun searchPasswords(query: String): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM categories ORDER BY id")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    @Query("UPDATE categories SET name = :newName WHERE name = :oldName")
    suspend fun renameCategory(oldName: String, newName: String)

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun countCategoriesWithName(name: String): Int

    @Query("SELECT COUNT(*) FROM password_entries WHERE category = :name")
    suspend fun countPasswordsInCategory(name: String): Int

    @Query("UPDATE password_entries SET category = :newName WHERE category = :oldName")
    suspend fun reassignPasswords(oldName: String, newName: String)

    @Query("DELETE FROM password_entries WHERE category = :name")
    suspend fun deletePasswordsByCategory(name: String)
}
