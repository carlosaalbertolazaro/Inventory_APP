package com.example.appcrud.data.mapper

import com.example.appcrud.data.dto.EquipoDto
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.model.EquipoEstado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EquipoMapperTest {
    @Test
    fun `convierte DTO completo a dominio`() {
        val equipo = EquipoMapper.toDomain(
            EquipoDto(
                id = "7",
                numeroSerie = "SN-007",
                marca = "HP",
                modelo = "ProDesk",
                fechaAdquisicion = "2024-01-10",
                estado = "EN_USO",
                numeroInventario = 1007,
                ubicacion = "Laboratorio"
            )
        )

        assertEquals("7", equipo.id)
        assertEquals(EquipoEstado.EN_USO, equipo.estado)
        assertEquals(1007, equipo.numeroInventario)
    }

    @Test
    fun `usa estado disponible cuando el servidor envía un valor desconocido`() {
        val equipo = EquipoMapper.toDomain(EquipoDto(estado = "ESTADO_FUTURO"))

        assertEquals(EquipoEstado.DISPONIBLE, equipo.estado)
    }

    @Test
    fun `no envía un id vacío al crear un equipo`() {
        val dto = EquipoMapper.toDto(Equipo(id = "", numeroSerie = "SN-NUEVO"))

        assertNull(dto.id)
        assertEquals("SN-NUEVO", dto.numeroSerie)
    }
}
