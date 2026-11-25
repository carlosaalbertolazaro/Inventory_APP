package com.example.appcrud.domain.repository

import com.example.appcrud.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String): Result<AuthSession>
    fun hasActiveSession(): Boolean
    fun logout()
}
