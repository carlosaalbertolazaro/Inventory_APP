package com.example.appcrud.data.mapper

import com.example.appcrud.data.dto.EquipoDto
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.model.EquipoEstado

object EquipoMapper {
    fun toDomain(dto: EquipoDto): Equipo {
        return Equipo(
            id = dto.id ?: "",
            numeroSerie = dto.numeroSerie ?: "",
            marca = dto.marca ?: "",
            modelo = dto.modelo ?: "",
            fechaAdquisicion = dto.fechaAdquisicion ?: "",
            estado = EquipoEstado.entries.firstOrNull { estado ->
                estado.name == dto.estado?.uppercase()
            } ?: EquipoEstado.DISPONIBLE,
            numeroInventario = dto.numeroInventario ?: 0,
            ubicacion = dto.ubicacion ?: ""
        )
    }

    fun toDto(equipo: Equipo): EquipoDto {
        return EquipoDto(
            id = equipo.id.takeIf { it.isNotBlank() },
            numeroSerie = equipo.numeroSerie.takeIf { it.isNotBlank() },
            marca = equipo.marca.takeIf { it.isNotBlank() },
            modelo = equipo.modelo.takeIf { it.isNotBlank() },
            fechaAdquisicion = equipo.fechaAdquisicion.takeIf { it.isNotBlank() },
            estado = equipo.estado.name,
            numeroInventario = equipo.numeroInventario.takeIf { it > 0 },
            ubicacion = equipo.ubicacion.takeIf { it.isNotBlank() }
        )
    }
}