package com.example.appcrud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcrud.data.local.SessionStorage
import com.example.appcrud.data.remote.ApiService
import com.example.appcrud.data.remote.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val apiService: ApiService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response: Response<AuthResponse> = apiService.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.accessToken
                    val userEmail = response.body()!!.user?.email ?: email
                    sessionStorage.saveSession(token, userEmail)
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Credenciales inválidas")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error de red")
            }
        }
    }

    fun checkSession() {
        _uiState.value = _uiState.value.copy(isLoggedIn = sessionStorage.isLoggedIn())
    }

    fun logout() {
        sessionStorage.clearSession()
        _uiState.value = LoginUiState()
    }
}