package com.example.appcrud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.repository.AuthRepository
import com.example.appcrud.domain.repository.EquipoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val equipos: List<Equipo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSessionClosed: Boolean = false
)

class HomeViewModel(
    private val equipoRepository: EquipoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun listarEquipos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            cargarEquipos()
        }
    }

    fun crearEquipo(equipo: Equipo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            equipoRepository.crearEquipo(equipo).fold(
                onSuccess = { cargarEquipos() },
                onFailure = { mostrarError(it, "No se pudo crear el equipo.") }
            )
        }
    }

    fun actualizarEquipo(id: String, equipo: Equipo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            equipoRepository.actualizarEquipo(id, equipo).fold(
                onSuccess = { cargarEquipos() },
                onFailure = { mostrarError(it, "No se pudo actualizar el equipo.") }
            )
        }
    }

    fun eliminarEquipo(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            equipoRepository.eliminarEquipo(id).fold(
                onSuccess = { cargarEquipos() },
                onFailure = { mostrarError(it, "No se pudo eliminar el equipo.") }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = _uiState.value.copy(isSessionClosed = true)
    }

    private suspend fun cargarEquipos() {
        equipoRepository.listarEquipos().fold(
            onSuccess = { equipos ->
                _uiState.value = _uiState.value.copy(
                    equipos = equipos,
                    isLoading = false,
                    error = null
                )
            },
            onFailure = { mostrarError(it, "No se pudieron cargar los equipos.") }
        )
    }

    private fun mostrarError(exception: Throwable, fallback: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = exception.message ?: fallback
        )
    }
}
