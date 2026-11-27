package com.example.appcrud.core.hardware.camera.domain.usecase

import com.example.appcrud.core.hardware.camera.domain.model.CapturedPhoto
import com.example.appcrud.core.hardware.camera.domain.repository.CameraRepository
import javax.inject.Inject

class CreatePhotoUriUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
) {
    operator fun invoke(): Result<CapturedPhoto> = cameraRepository.createPhotoUri()
}
