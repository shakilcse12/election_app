package com.example.electionapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // New Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onUsernameChange(newValue: String) { _username.value = newValue }
    fun onPasswordChange(newValue: String) { _password.value = newValue }

    fun login(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulating a quick network check

            val success = _username.value == "admin" && _password.value == "admin123"
            if (success) {
                _isLoggedIn.value = true
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _username.value = ""
        _password.value = ""
    }
}