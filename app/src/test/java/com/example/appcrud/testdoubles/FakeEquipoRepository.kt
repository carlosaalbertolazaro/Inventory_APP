package com.example.appcrud.testdoubles

import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.repository.EquipoRepository

class FakeEquipoRepository(
    val equipos: MutableList<Equipo> = mutableListOf()
) : EquipoRepository {
    var listError: Throwable? = null
    var createError: Throwable? = null
    var listCalls: Int = 0

    override suspend fun listarEquipos(): Result<List<Equipo>> {
        listCalls++
        return listError?.let { Result.failure(it) } ?: Result.success(equipos.toList())
    }

    override suspend fun obtenerEquipo(id: String): Result<Equipo> {
        val equipo = equipos.find { it.id == id }
        return equipo?.let { Result.success(it) } ?: Result.failure(Exception("No encontrado"))
    }

    override suspend fun crearEquipo(equipo: Equipo): Result<Equipo> {
        createError?.let { return Result.failure(it) }
        val creado = equipo.copy(id = equipo.id.ifBlank { (equipos.size + 1).toString() })
        equipos.add(creado)
        return Result.success(creado)
    }

    override suspend fun actualizarEquipo(id: String, equipo: Equipo): Result<Equipo> {
        val index = equipos.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(Exception("No encontrado"))
        val actualizado = equipo.copy(id = id)
        equipos[index] = actualizado
        return Result.success(actualizado)
    }

    override suspend fun eliminarEquipo(id: String): Result<Unit> {
        val eliminado = equipos.removeAll { it.id == id }
        return if (eliminado) Result.success(Unit) else Result.failure(Exception("No encontrado"))
    }
}
