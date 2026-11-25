package com.example.appcrud.data.repository

import com.example.appcrud.data.dto.AuthRequest
import com.example.appcrud.data.dto.AuthResponse
import com.example.appcrud.data.local.SessionStorage
import com.example.appcrud.data.remote.ApiCallException
import com.example.appcrud.data.remote.ApiService
import com.example.appcrud.data.remote.apiResult
import com.example.appcrud.domain.model.AuthSession
import com.example.appcrud.domain.repository.AuthRepository

class AuthRepositoryImpl internal constructor(
    private val apiService: ApiService,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return authenticate("Correo o contraseña incorrectos.", email) {
            apiService.login(AuthRequest(email, password))
        }
    }

    override suspend fun register(email: String, password: String): Result<AuthSession> {
        return authenticate(
            "No se pudo registrar la cuenta. Verifica los datos o usa otro correo.",
            email
        ) {
            apiService.register(AuthRequest(email, password))
        }
    }

    private suspend fun authenticate(
        errorMessage: String,
        email: String,
        call: suspend () -> AuthResponse
    ): Result<AuthSession> {
        return apiResult(errorMessage, call).fold(
            onSuccess = { response ->
                val token = response.accessToken
                if (token.isNullOrBlank()) {
                    Result.failure(ApiCallException("La respuesta del servidor no contiene una sesión válida."))
                } else {
                    AuthSession(
                        token = token,
                        email = response.user?.email ?: email
                    ).also { session ->
                        sessionStorage.saveSession(session.token, session.email)
                    }.let { Result.success(it) }
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    override fun hasActiveSession(): Boolean = sessionStorage.isLoggedIn()

    override fun logout() = sessionStorage.clearSession()
}
