package com.example.mavpulse.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.LoginRequest
import com.example.mavpulse.network.RegisterRequest
import com.example.mavpulse.network.RetrofitInstance
import com.example.mavpulse.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.api
    private val sessionManager = SessionManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _loggedInUsername = mutableStateOf<String?>(null)
    val loggedInUsername: State<String?> = _loggedInUsername

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    init {
        _loggedInUsername.value = sessionManager.getUsername()
        _userId.value = sessionManager.getUserId()
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(response.body()?.message ?: "Registration successful")
                } else {
                    _authState.value = AuthState.Error(response.errorBody()?.string() ?: "Registration failed")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed", e)
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body()?.token != null && response.body()?.userId != null) {
                    val body = response.body()!!
                    sessionManager.saveAuthToken(body.token!!)
                    sessionManager.saveUserId(body.userId!!)
                    val username = body.username ?: email.split("@").firstOrNull() ?: "User"
                    sessionManager.saveUsername(username)
                    _loggedInUsername.value = username
                    _userId.value = body.userId
                    _authState.value = AuthState.Success("Login successful")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(errorBody ?: "Invalid credentials")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _loggedInUsername.value = null
        _userId.value = null
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
