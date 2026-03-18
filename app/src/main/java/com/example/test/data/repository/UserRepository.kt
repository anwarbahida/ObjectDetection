package com.example.test.data.repository

import com.example.test.data.model.User
import com.example.test.data.network.RetrofitInstance

class UserRepository {

    private val api = RetrofitInstance.api

    // ─────────────────────────────────────────
    // Récupérer tous les users
    // ─────────────────────────────────────────
    suspend fun getUsers() = api.getUsers()

    // ─────────────────────────────────────────
    // Vérifier la connexion
    // ─────────────────────────────────────────
    suspend fun checkLogin(email: String, password: String): Boolean {
        val users = api.getUsers()
        return users.any { user ->
            (user.email == email || user.username == email)
                    && user.name == password
        }
    }

    // ─────────────────────────────────────────
    // Vérifier si user existe déjà (Register)
    // ─────────────────────────────────────────
    suspend fun checkUserExists(email: String, username: String): Boolean {
        val users = api.getUsers()
        return users.any {
            it.email == email || it.username == username
        }
    }

    // Dans UserRepository.kt — ajouter cette fonction
    suspend fun getUserByLogin(email: String, password: String): User? {
        val users = api.getUsers()
        return users.find { user ->
            (user.email == email || user.username == email)
                    && user.name == password
        }
    }


}