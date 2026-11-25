package com.example.appcrud.data.repository

import com.example.appcrud.data.dto.AuthRequest
import com.example.appcrud.data.dto.AuthResponse
import com.example.appcrud.data.dto.EquipoDto
import com.example.appcrud.data.remote.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException

class EquipoRepositoryImplTest {
    @Test
    fun `un error HTTP no se convierte en una lista vacía exitosa`() = runTest {
        val apiService = FakeApiService()
        apiService.onListar = {
            val response = Response.error<List<EquipoDto>>(
                500,
                "{}".toResponseBody("application/json".toMediaType())
            )
            throw HttpException(response)
        }

        val result = EquipoRepositoryImpl(apiService).listarEquipos()

        assertTrue(result.isFailure)
        assertEquals(
            "El servidor no está disponible. Inténtalo más tarde.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `un fallo de conexión tiene un mensaje accionable`() = runTest {
        val apiService = FakeApiService()
        apiService.onListar = { throw UnknownHostException("api-no-disponible") }

        val result = EquipoRepositoryImpl(apiService).listarEquipos()

        assertTrue(result.isFailure)
        assertEquals(
            "No se pudo conectar con el servidor. Verifica que la API esté activa y la URL sea correcta.",
            result.exceptionOrNull()?.message
        )
    }

    private class FakeApiService : ApiService {
        var onListar: suspend () -> List<EquipoDto> = { emptyList() }

        override suspend fun listarEquipos(): List<EquipoDto> = onListar()

        override suspend fun obtenerEquipo(id: String): EquipoDto {
            error("No utilizado en esta prueba")
        }

        override suspend fun crearEquipo(equipo: EquipoDto): EquipoDto {
            error("No utilizado en esta prueba")
        }

        override suspend fun actualizarEquipo(id: String, equipo: EquipoDto): EquipoDto {
            error("No utilizado en esta prueba")
        }

        override suspend fun eliminarEquipo(id: String) {
            error("No utilizado en esta prueba")
        }

        override suspend fun register(request: AuthRequest): AuthResponse {
            error("No utilizado en esta prueba")
        }

        override suspend fun login(request: AuthRequest): AuthResponse {
            error("No utilizado en esta prueba")
        }
    }
}
