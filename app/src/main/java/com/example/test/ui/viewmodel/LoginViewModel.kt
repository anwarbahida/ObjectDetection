package com.example.test.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.User
import com.example.test.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = UserRepository()

    var email    by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    sealed class LoginState {
        object Idle    : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    var loginState by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    fun onEmailChange(newEmail: String)       { email = newEmail }
    fun onPasswordChange(newPassword: String) { password = newPassword }

    // ← context passé depuis le Screen
    fun login(context: Context) {
        if (email.isBlank() || password.isBlank()) {
            loginState = LoginState.Error("Veuillez remplir tous les champs")
            return
        }

        viewModelScope.launch {
            loginState = LoginState.Loading
            try {
                // ← récupère l'objet User complet
                val user = repository.getUserByLogin(email, password)

                if (user != null) {
                    // ← sauvegarde dans SharedPreferences
                    saveUser(context, user)
                    loginState = LoginState.Success
                } else {
                    loginState = LoginState.Error("Identifiants incorrects")
                }

            } catch (e: Exception) {
                loginState = LoginState.Error("Erreur réseau : ${e.message}")
            }
        }
    }

    // ← sauvegarde toutes les infos
    private fun saveUser(context: Context, user: User) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt   ("user_id",  user.id)
            .putString("name",     user.name)
            .putString("email",    user.email)
            .putString("username", user.username)
            .putString("phone",    user.phone)
            .putString("website",  user.website)
            .putString("company",  user.company.name)
            .putString("city",     user.address.city)
            .apply()
    }

    fun resetState() {
        loginState = LoginState.Idle
    }
}