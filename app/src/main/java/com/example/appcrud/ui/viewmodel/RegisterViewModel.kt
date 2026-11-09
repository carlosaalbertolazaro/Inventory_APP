package com.example.appcrud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcrud.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false,
    val email: String = "",
    val password: String = ""
)

class RegisterViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun register() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Llena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response: Response<Void> = apiService.register(email, password)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, registered = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al registrar")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error de red")
            }
        }
    }
}