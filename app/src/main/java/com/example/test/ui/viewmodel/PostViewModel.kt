package com.example.test.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.Post
import com.example.test.data.repository.PostRespository
import kotlinx.coroutines.launch

class PostViewModel: ViewModel() {

    private val repository = PostRespository()

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            isLoading = true
            try {
                posts = repository.getPosts()
            } catch (e: Exception) {
                errorMessage = "Erreur : ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

}