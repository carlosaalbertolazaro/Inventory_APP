package com.example.appcrud.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialValidatorTest {
    @Test
    fun `rechaza campos vacíos`() {
        assertEquals("Completa todos los campos.", CredentialValidator.validateLogin("", ""))
    }

    @Test
    fun `rechaza correo inválido`() {
        assertEquals(
            "Ingresa un correo electrónico válido.",
            CredentialValidator.validateLogin("correo-invalido", "clave123")
        )
    }

    @Test
    fun `rechaza contraseña menor al mínimo del backend`() {
        assertEquals(
            "La contraseña debe tener al menos 4 caracteres.",
            CredentialValidator.validateLogin("prueba@correo.com", "abc")
        )
    }

    @Test
    fun `rechaza confirmación diferente`() {
        assertEquals(
            "Las contraseñas no coinciden.",
            CredentialValidator.validateRegistration("prueba@correo.com", "clave123", "otra123")
        )
    }

    @Test
    fun `acepta registro válido`() {
        assertNull(
            CredentialValidator.validateRegistration("prueba@correo.com", "clave123", "clave123")
        )
    }
}
