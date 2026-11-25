package com.example.appcrud.ui.viewmodel

import com.example.appcrud.MainDispatcherRule
import com.example.appcrud.testdoubles.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `registro válido habilita la navegación autenticada`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(repository)
        viewModel.onEmailChange("nuevo@correo.com")
        viewModel.onPasswordChange("clave123")
        viewModel.onConfirmPasswordChange("clave123")

        viewModel.register()

        assertTrue(viewModel.uiState.value.registered)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, repository.registerCalls)
        assertEquals("nuevo@correo.com", repository.lastEmail)
    }

    @Test
    fun `contraseñas diferentes no llaman a la API`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = RegisterViewModel(repository)
        viewModel.onEmailChange("nuevo@correo.com")
        viewModel.onPasswordChange("clave123")
        viewModel.onConfirmPasswordChange("diferente")

        viewModel.register()

        assertFalse(viewModel.uiState.value.registered)
        assertEquals("Las contraseñas no coinciden.", viewModel.uiState.value.error)
        assertEquals(0, repository.registerCalls)
    }
}
