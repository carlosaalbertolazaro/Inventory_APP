package com.example.appcrud.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

internal class ApiCallException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)

internal suspend fun <T> apiResult(
    errorMessage: String,
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(call())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: HttpException) {
        Result.failure(
            ApiCallException(
                message = exception.toUserMessage(errorMessage),
                cause = exception
            )
        )
    } catch (exception: IOException) {
        Result.failure(
            ApiCallException(
                message = "No se pudo conectar con el servidor. Verifica que la API esté activa y la URL sea correcta.",
                cause = exception
            )
        )
    } catch (exception: Exception) {
        Result.failure(
            ApiCallException(
                message = "No se pudo procesar la respuesta del servidor.",
                cause = exception
            )
        )
    }
}

private fun HttpException.toUserMessage(fallback: String): String {
    return when (code()) {
        400 -> fallback
        401 -> "La sesión expiró o no es válida. Inicia sesión nuevamente."
        403 -> "No tienes permisos para realizar esta operación."
        404 -> "El recurso solicitado no existe."
        409 -> "Ya existe un equipo con el mismo número de serie o inventario."
        in 500..599 -> "El servidor no está disponible. Inténtalo más tarde."
        else -> "$fallback (HTTP ${code()})"
    }
}
