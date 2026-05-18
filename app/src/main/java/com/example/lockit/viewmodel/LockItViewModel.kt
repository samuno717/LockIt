package com.example.lockit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.lockit.data.LockItRepository
import com.example.lockit.model.PasswordEntry
import com.example.lockit.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockItViewModel(private val repository: LockItRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = repository.getUser()
            _currentUser.value = user
            if (user != null) {
                repository.insertUser(user.copy(lastLogin = System.currentTimeMillis()))
            }
        }
    }

    fun registerUser(username: String, passkey: String) {
        viewModelScope.launch {
            val user = User(username = username, passkey = passkey)
            repository.insertUser(user)
            _currentUser.value = user
        }
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
