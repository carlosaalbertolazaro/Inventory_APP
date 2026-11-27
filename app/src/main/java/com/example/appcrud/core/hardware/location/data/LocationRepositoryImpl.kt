package com.example.appcrud.core.hardware.location.data

import com.example.appcrud.core.hardware.location.domain.model.DeviceLocation
import com.example.appcrud.core.hardware.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dataSource: LocationDataSource,
) : LocationRepository {
    override suspend fun getCurrentLocation(): Result<DeviceLocation> =
        dataSource.getCurrentLocation()

    override fun observeLocation(): Flow<Result<DeviceLocation>> = dataSource.observeLocation()
}
