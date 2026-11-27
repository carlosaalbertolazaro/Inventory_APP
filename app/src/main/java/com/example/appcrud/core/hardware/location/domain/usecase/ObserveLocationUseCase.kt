package com.example.appcrud.core.hardware.location.domain.usecase

import com.example.appcrud.core.hardware.location.domain.model.DeviceLocation
import com.example.appcrud.core.hardware.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    operator fun invoke(): Flow<Result<DeviceLocation>> = locationRepository.observeLocation()
}
