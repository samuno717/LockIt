package com.example.lockit.data

import com.example.lockit.data.local.LockItDao
import com.example.lockit.model.Category
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User
import com.example.lockit.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LockItRepository(private val dao: LockItDao) {
    // The `password` field is encrypted at rest; decrypt on the way out, encrypt on the way in.
    val allPasswords: Flow<List<PasswordEntry>> =
        dao.getAllPasswords().map { list -> list.map { it.decryptedPassword() } }
    val allCategories: Flow<List<Category>> = dao.getAllCategories()

    suspend fun getUser(): User? = dao.getUser()
    suspend fun insertUser(user: User) = dao.insertUser(user)

    suspend fun insertPassword(entry: PasswordEntry) = dao.insertPassword(entry.encryptedPassword())
    suspend fun deletePassword(entry: PasswordEntry) = dao.deletePassword(entry)
    suspend fun getPasswordById(id: Long): PasswordEntry? = dao.getPasswordById(id)?.decryptedPassword()

    fun searchPasswords(query: String): Flow<List<PasswordEntry>> =
        dao.searchPasswords(query).map { list -> list.map { it.decryptedPassword() } }

    /** One-time pass that encrypts any rows still stored as plaintext. */
    suspend fun encryptLegacyPasswords() {
        dao.getAllPasswordsOnce().forEach { entry ->
            if (!CryptoManager.isEncrypted(entry.password)) {
                dao.insertPassword(entry.encryptedPassword())
            }
        }
    }

    private fun PasswordEntry.encryptedPassword(): PasswordEntry =
        if (CryptoManager.isEncrypted(password)) this
        else copy(password = CryptoManager.encrypt(password))

    private fun PasswordEntry.decryptedPassword(): PasswordEntry =
        copy(password = CryptoManager.decrypt(password))

    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)
    suspend fun deleteCategoryByName(name: String) = dao.deleteCategoryByName(name)
    suspend fun renameCategory(oldName: String, newName: String) = dao.renameCategory(oldName, newName)
    suspend fun categoryExists(name: String): Boolean = dao.countCategoriesWithName(name) > 0
    suspend fun countPasswordsInCategory(name: String): Int = dao.countPasswordsInCategory(name)
    suspend fun reassignPasswords(oldName: String, newName: String) = dao.reassignPasswords(oldName, newName)
    suspend fun deletePasswordsByCategory(name: String) = dao.deletePasswordsByCategory(name)
}
