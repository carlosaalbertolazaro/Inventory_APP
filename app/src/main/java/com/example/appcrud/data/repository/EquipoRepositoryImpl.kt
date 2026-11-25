package com.example.appcrud.data.repository

import com.example.appcrud.data.mapper.EquipoMapper
import com.example.appcrud.data.remote.ApiService
import com.example.appcrud.data.remote.apiResult
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.repository.EquipoRepository

class EquipoRepositoryImpl internal constructor(
    private val apiService: ApiService
) : EquipoRepository {

    override suspend fun listarEquipos(): Result<List<Equipo>> {
        return apiResult("No se pudieron cargar los equipos.") {
            apiService.listarEquipos().map(EquipoMapper::toDomain)
        }
    }

    override suspend fun obtenerEquipo(id: String): Result<Equipo> {
        return apiResult("No se pudo obtener el equipo.") {
            EquipoMapper.toDomain(apiService.obtenerEquipo(id))
        }
    }

    override suspend fun crearEquipo(equipo: Equipo): Result<Equipo> {
        return apiResult("No se pudo crear el equipo.") {
            val dto = EquipoMapper.toDto(equipo)
            EquipoMapper.toDomain(apiService.crearEquipo(dto))
        }
    }

    override suspend fun actualizarEquipo(id: String, equipo: Equipo): Result<Equipo> {
        return apiResult("No se pudo actualizar el equipo.") {
            val dto = EquipoMapper.toDto(equipo)
            EquipoMapper.toDomain(apiService.actualizarEquipo(id, dto))
        }
    }

    override suspend fun eliminarEquipo(id: String): Result<Unit> {
        return apiResult("No se pudo eliminar el equipo.") {
            apiService.eliminarEquipo(id)
        }
    }
}
