package com.example.appcrud.data.dto

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String? = null,
    val user: AuthUserDto? = null
)

data class AuthUserDto(
    val email: String? = null
)
