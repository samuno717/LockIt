package com.example.lockit.data

import com.example.lockit.data.local.LockItDao
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User
import kotlinx.coroutines.flow.Flow

class LockItRepository(private val dao: LockItDao) {
    val allPasswords: Flow<List<PasswordEntry>> = dao.getAllPasswords()

    suspend fun getUser(): User? = dao.getUser()
    suspend fun insertUser(user: User) = dao.insertUser(user)

    suspend fun insertPassword(entry: PasswordEntry) = dao.insertPassword(entry)
    suspend fun deletePassword(entry: PasswordEntry) = dao.deletePassword(entry)
    suspend fun getPasswordById(id: Long): PasswordEntry? = dao.getPasswordById(id)
    
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> = dao.searchPasswords(query)
}
