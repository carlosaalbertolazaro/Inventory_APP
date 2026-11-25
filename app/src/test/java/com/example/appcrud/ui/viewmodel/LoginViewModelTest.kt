package com.example.appcrud.ui.viewmodel

import com.example.appcrud.MainDispatcherRule
import com.example.appcrud.testdoubles.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `login válido actualiza la sesión y limpia el loading`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange("  prueba@correo.com  ")
        viewModel.onPasswordChange("clave123")

        viewModel.login()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertFalse(state.isLoading)
        assertEquals("prueba@correo.com", repository.lastEmail)
        assertEquals(1, repository.loginCalls)
    }

    @Test
    fun `login inválido no llama a la API`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange("correo-invalido")
        viewModel.onPasswordChange("clave123")

        viewModel.login()

        assertEquals("Ingresa un correo electrónico válido.", viewModel.uiState.value.error)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `error del repositorio se muestra sin marcar sesión activa`() = runTest {
        val repository = FakeAuthRepository().apply {
            loginResult = Result.failure(Exception("Credenciales inválidas"))
        }
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChange("prueba@correo.com")
        viewModel.onPasswordChange("incorrecta")

        viewModel.login()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals("Credenciales inválidas", viewModel.uiState.value.error)
    }
}
