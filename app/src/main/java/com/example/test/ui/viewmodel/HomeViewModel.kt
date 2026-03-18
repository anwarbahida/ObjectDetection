package com.example.test.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.User
import com.example.test.data.repository.UserRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
class HomeViewModel : ViewModel() {

    private val repository = UserRepository()

    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            isLoading = true
            try {
                users = repository.getUsers()
            } catch (e: Exception) {
                errorMessage = "Erreur : ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
