package com.example.appcrud.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EquipoValidatorTest {
    @Test
    fun `rechaza formulario incompleto`() {
        assertEquals("Completa todos los campos.", validate(numeroSerie = ""))
    }

    @Test
    fun `rechaza inventario que no sea entero positivo`() {
        assertEquals(
            "El número de inventario debe ser un entero mayor que cero.",
            validate(numeroInventario = "0")
        )
    }

    @Test
    fun `rechaza fecha imposible`() {
        assertEquals(
            "La fecha debe tener un formato válido: AAAA-MM-DD.",
            validate(fechaAdquisicion = "2023-02-29")
        )
    }

    @Test
    fun `acepta fecha de año bisiesto y formulario válido`() {
        assertNull(validate(fechaAdquisicion = "2024-02-29"))
    }

    @Test
    fun `acepta fecha válida con espacios accidentales`() {
        assertNull(validate(fechaAdquisicion = "  2024-01-15  "))
    }

    private fun validate(
        numeroSerie: String = "SN-001",
        marca: String = "Dell",
        modelo: String = "OptiPlex",
        fechaAdquisicion: String = "2024-01-15",
        numeroInventario: String = "1001",
        ubicacion: String = "Oficina"
    ): String? {
        return EquipoValidator.validate(
            numeroSerie,
            marca,
            modelo,
            fechaAdquisicion,
            numeroInventario,
            ubicacion
        )
    }
}
