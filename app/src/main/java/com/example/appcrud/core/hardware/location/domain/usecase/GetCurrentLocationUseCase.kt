package com.example.appcrud.core.hardware.location.domain.usecase

import com.example.appcrud.core.hardware.location.domain.model.DeviceLocation
import com.example.appcrud.core.hardware.location.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): Result<DeviceLocation> =
        locationRepository.getCurrentLocation()
}
