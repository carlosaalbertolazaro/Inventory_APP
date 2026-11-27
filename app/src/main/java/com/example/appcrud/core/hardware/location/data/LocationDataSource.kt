package com.example.appcrud.core.hardware.location.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.appcrud.core.hardware.location.domain.model.DeviceLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationDataSource @Inject constructor(
    context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    private val appContext = context.applicationContext

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<DeviceLocation> {
        if (!hasLocationPermission()) return missingPermissionResult()

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token,
                ).addOnSuccessListener { location ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    val result = location?.toDeviceLocation()?.let { Result.success(it) }
                        ?: Result.failure(
                            IllegalStateException("No hay una ubicación actual disponible"),
                        )
                    continuation.resume(result)
                }.addOnFailureListener { error ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(error))
                    }
                }
            } catch (error: Exception) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(error))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun observeLocation(): Flow<Result<DeviceLocation>> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(missingPermissionResult())
            close()
            return@callbackFlow
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(Result.success(location.toDeviceLocation()))
                }
            }
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS,
        ).setMinUpdateIntervalMillis(MIN_LOCATION_UPDATE_INTERVAL_MS).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper(),
            ).addOnFailureListener { error ->
                trySend(Result.failure(error))
                close()
            }
        } catch (error: Exception) {
            trySend(Result.failure(error))
            close()
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(appContext, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    private fun missingPermissionResult(): Result<DeviceLocation> = Result.failure(
        SecurityException("Permiso de ubicación no concedido"),
    )

    private fun Location.toDeviceLocation(): DeviceLocation = DeviceLocation(
        latitude = latitude,
        longitude = longitude,
    )

    private companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        const val LOCATION_UPDATE_INTERVAL_MS = 10_000L
        const val MIN_LOCATION_UPDATE_INTERVAL_MS = 5_000L
    }
}
