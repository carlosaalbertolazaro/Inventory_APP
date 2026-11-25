package com.example.appcrud.domain.validation

object CredentialValidator {
    const val MIN_PASSWORD_LENGTH = 4
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateLogin(email: String, password: String): String? {
        return validateCredentials(email, password)
    }

    fun validateRegistration(
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return validateCredentials(email, password)
            ?: if (password != confirmPassword) "Las contraseñas no coinciden." else null
    }

    private fun validateCredentials(email: String, password: String): String? {
        return when {
            email.isBlank() || password.isBlank() -> "Completa todos los campos."
            !emailPattern.matches(email.trim()) -> "Ingresa un correo electrónico válido."
            password.length < MIN_PASSWORD_LENGTH ->
                "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."
            else -> null
        }
    }
}
