package com.example.appcrud.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcrud.data.repository.EquipoRepositoryImpl
import com.example.appcrud.domain.model.Equipo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.Result

data class HomeUiState(
    val equipos: List<Equipo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val equipoRepository: EquipoRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private fun updateLoading(loading: Boolean, error: String? = null) {
        _uiState.value = _uiState.value.copy(isLoading = loading, error = error)
    }

    fun listarEquipos() {
        viewModelScope.launch {
            updateLoading(true)
            val result = equipoRepository.listarEquipos()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, equipos = result.getOrDefault(emptyList()))
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al cargar equipos")
            }
        }
    }

    fun crearEquipo(equipo: Equipo) {
        viewModelScope.launch {
            updateLoading(true)
            val result = equipoRepository.crearEquipo(equipo)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                listarEquipos()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al crear")
            }
        }
    }

    fun actualizarEquipo(id: String, equipo: Equipo) {
        viewModelScope.launch {
            updateLoading(true)
            val result = equipoRepository.actualizarEquipo(id, equipo)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                listarEquipos()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    fun eliminarEquipo(id: String) {
        viewModelScope.launch {
            updateLoading(true)
            val result = equipoRepository.eliminarEquipo(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                listarEquipos()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }
}