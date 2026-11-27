package com.example.appcrud.core.hardware.camera.domain.usecase

import com.example.appcrud.core.hardware.camera.domain.repository.CameraRepository
import javax.inject.Inject

class DeleteTempPhotoUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
) {
    operator fun invoke(filePath: String): Result<Boolean> =
        cameraRepository.deleteTempPhoto(filePath)

    fun deleteOrphans(retainedFilePaths: Set<String> = emptySet()): Result<Int> =
        cameraRepository.deleteOrphanedTempPhotos(retainedFilePaths)
}
