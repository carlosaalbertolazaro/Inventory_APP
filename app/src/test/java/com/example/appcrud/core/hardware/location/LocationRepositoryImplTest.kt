package com.example.appcrud.core.hardware.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.appcrud.core.hardware.location.data.LocationDataSource
import com.example.appcrud.core.hardware.location.data.LocationRepositoryImpl
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setUp() {
        context = mockk()
        fusedLocationClient = mockk()
        every { context.applicationContext } returns context
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_DENIED
        repository = LocationRepositoryImpl(LocationDataSource(context, fusedLocationClient))
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun `consulta puntual devuelve SecurityException cuando faltan permisos`() = runTest {
        val result = repository.getCurrentLocation()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertEquals(
            "Permiso de ubicación no concedido",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `flujo devuelve SecurityException cuando faltan permisos`() = runTest {
        val result = repository.observeLocation().first()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertEquals(
            "Permiso de ubicación no concedido",
            result.exceptionOrNull()?.message,
        )
    }
}
