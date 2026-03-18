package com.example.test.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.test.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoggedUser(
    val id      : Int    = 0,
    val name    : String = "",
    val email   : String = "",
    val username: String = "",
    val phone   : String = "",
    val website : String = "",
    val company : String = "",
    val city    : String = ""
)

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(
        "user_prefs", Context.MODE_PRIVATE
    )

    private val _user = MutableStateFlow(LoggedUser())
    val user: StateFlow<LoggedUser> = _user.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        _user.value = LoggedUser(
            id       = prefs.getInt   ("user_id",  0),
            name     = prefs.getString("name",     "") ?: "",
            email    = prefs.getString("email",    "") ?: "",
            username = prefs.getString("username", "") ?: "",
            phone    = prefs.getString("phone",    "") ?: "",
            website  = prefs.getString("website",  "") ?: "",
            company  = prefs.getString("company",  "") ?: "",
            city     = prefs.getString("city",     "") ?: ""
        )
    }

    fun logout() {
        prefs.edit().clear().apply()
        _user.value = LoggedUser()
    }
}