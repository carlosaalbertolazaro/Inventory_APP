package com.example.appcrud.core.hardware.location.domain.repository

import com.example.appcrud.core.hardware.location.domain.model.DeviceLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<DeviceLocation>

    fun observeLocation(): Flow<Result<DeviceLocation>>
}
