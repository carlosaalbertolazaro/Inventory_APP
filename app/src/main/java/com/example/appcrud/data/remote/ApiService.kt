package com.example.appcrud.data.remote

import com.example.appcrud.data.dto.AuthRequest
import com.example.appcrud.data.dto.AuthResponse
import com.example.appcrud.data.dto.EquipoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("equipos")
    suspend fun listarEquipos(): List<EquipoDto>

    @GET("equipos/{id}")
    suspend fun obtenerEquipo(@Path("id") id: String): EquipoDto

    @POST("equipos")
    suspend fun crearEquipo(@Body equipo: EquipoDto): EquipoDto

    @PATCH("equipos/{id}")
    suspend fun actualizarEquipo(
        @Path("id") id: String,
        @Body equipo: EquipoDto
    ): EquipoDto

    @DELETE("equipos/{id}")
    suspend fun eliminarEquipo(@Path("id") id: String)

    @POST("register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}
