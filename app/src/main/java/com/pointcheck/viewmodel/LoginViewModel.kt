package com.pointcheck.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isValid: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onValueChange(field: String, value: String) {
        val s = _uiState.value
        _uiState.value = when(field) {
            "email" -> s.copy(email = value, isValid = isValid(value, s.password))
            "password" -> s.copy(password = value, isValid = isValid(s.email, value))
            else -> s
        }
    }
    private fun isValid(email: String, pass: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && pass.length >= 6
}
