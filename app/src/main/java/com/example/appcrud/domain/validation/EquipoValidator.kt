package com.example.appcrud.domain.validation

import java.time.LocalDate
import java.time.format.DateTimeParseException

object EquipoValidator {
    fun validate(
        numeroSerie: String,
        marca: String,
        modelo: String,
        fechaAdquisicion: String,
        numeroInventario: String,
        ubicacion: String
    ): String? {
        if (listOf(numeroSerie, marca, modelo, fechaAdquisicion, numeroInventario, ubicacion)
                .any { it.isBlank() }
        ) {
            return "Completa todos los campos."
        }

        val inventario = numeroInventario.toIntOrNull()
        if (inventario == null || inventario <= 0) {
            return "El número de inventario debe ser un entero mayor que cero."
        }

        if (!isValidDate(fechaAdquisicion)) {
            return "La fecha debe tener un formato válido: AAAA-MM-DD."
        }

        return null
    }

    private fun isValidDate(value: String): Boolean {
        return try {
            LocalDate.parse(value.trim())
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }
}
