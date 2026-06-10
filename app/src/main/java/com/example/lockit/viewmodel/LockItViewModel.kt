package com.example.lockit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.lockit.data.LockItRepository
import com.example.lockit.model.Category
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User
import com.example.lockit.security.PasswordHasher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockItViewModel(private val repository: LockItRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val passwords: StateFlow<List<PasswordEntry>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allPasswords
            } else {
                repository.searchPasswords(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<String>> = repository.allCategories
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadUser()
        viewModelScope.launch {
            repository.encryptLegacyPasswords()
        }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.insertCategory(Category(name = trimmed))
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == oldName) return
        viewModelScope.launch {
            repository.reassignPasswords(oldName, trimmed)
            if (repository.categoryExists(trimmed)) {
                repository.deleteCategoryByName(oldName)
            } else {
                repository.renameCategory(oldName, trimmed)
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deletePasswordsByCategory(name)
            repository.deleteCategoryByName(name)
        }
    }

    fun getPasswordCount(name: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            onResult(repository.countPasswordsInCategory(name))
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = repository.getUser()
            _currentUser.value = user
            if (user != null) {
                repository.insertUser(user.copy(lastLogin = System.currentTimeMillis()))
            }
            _isLoading.value = false
        }
    }

    fun registerUser(username: String, passkey: String) {
        viewModelScope.launch {
            val user = User(username = username, passkey = PasswordHasher.hash(passkey))
            repository.insertUser(user)
            _currentUser.value = user
        }
    }

    fun verifyPasskey(input: String): Boolean {
        val user = _currentUser.value ?: return false
        val ok = PasswordHasher.verify(input, user.passkey)
        if (ok && PasswordHasher.needsUpgrade(user.passkey)) {
            viewModelScope.launch {
                val upgraded = user.copy(passkey = PasswordHasher.hash(input))
                repository.insertUser(upgraded)
                _currentUser.value = upgraded
            }
        }
        return ok
    }

    fun updateProfileImage(uri: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                val updated = user.copy(profileImage = uri)
                repository.insertUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun addPassword(entry: PasswordEntry) {
        viewModelScope.launch {
            repository.insertPassword(entry)
        }
    }

    fun deletePassword(entry: PasswordEntry) {
        viewModelScope.launch {
            repository.deletePassword(entry)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun resetMasterKey() {
        viewModelScope.launch {
            repository.getUser()?.let {
                _currentUser.value = null
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val database = com.example.lockit.data.local.LockItDatabase.getDatabase(application)
                val repository = com.example.lockit.data.LockItRepository(database.dao())
                return LockItViewModel(repository) as T
            }
        }
    }
}
