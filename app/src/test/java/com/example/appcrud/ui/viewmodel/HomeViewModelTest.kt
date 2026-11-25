package com.example.appcrud.ui.viewmodel

import com.example.appcrud.MainDispatcherRule
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.testdoubles.FakeAuthRepository
import com.example.appcrud.testdoubles.FakeEquipoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `listar equipos publica los datos recibidos`() = runTest {
        val equipo = Equipo(id = "1", numeroSerie = "SN-001")
        val repository = FakeEquipoRepository(mutableListOf(equipo))
        val viewModel = HomeViewModel(repository, FakeAuthRepository())

        viewModel.listarEquipos()

        assertEquals(listOf(equipo), viewModel.uiState.value.equipos)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `crear equipo recarga la lista en la misma operación`() = runTest {
        val repository = FakeEquipoRepository()
        val viewModel = HomeViewModel(repository, FakeAuthRepository())

        viewModel.crearEquipo(Equipo(numeroSerie = "SN-NUEVO"))

        assertEquals(1, repository.equipos.size)
        assertEquals(1, repository.listCalls)
        assertEquals("SN-NUEVO", viewModel.uiState.value.equipos.single().numeroSerie)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `fallo de carga conserva el error y detiene el loading`() = runTest {
        val repository = FakeEquipoRepository().apply {
            listError = Exception("Servidor no disponible")
        }
        val viewModel = HomeViewModel(repository, FakeAuthRepository())

        viewModel.listarEquipos()

        assertEquals("Servidor no disponible", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `logout cierra la sesión y emite navegación`() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = HomeViewModel(FakeEquipoRepository(), authRepository)

        viewModel.logout()

        assertTrue(viewModel.uiState.value.isSessionClosed)
        assertEquals(1, authRepository.logoutCalls)
    }
}
