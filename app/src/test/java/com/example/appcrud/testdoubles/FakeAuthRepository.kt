package com.example.appcrud.testdoubles

import com.example.appcrud.domain.model.AuthSession
import com.example.appcrud.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var loginResult: Result<AuthSession> = Result.success(AuthSession("token", "prueba@correo.com"))
    var registerResult: Result<AuthSession> = Result.success(AuthSession("token", "prueba@correo.com"))
    var hasSession: Boolean = false
    var loginCalls: Int = 0
    var registerCalls: Int = 0
    var logoutCalls: Int = 0
    var lastEmail: String? = null

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        loginCalls++
        lastEmail = email
        return loginResult
    }

    override suspend fun register(email: String, password: String): Result<AuthSession> {
        registerCalls++
        lastEmail = email
        return registerResult
    }

    override fun hasActiveSession(): Boolean = hasSession

    override fun logout() {
        logoutCalls++
        hasSession = false
    }
}
