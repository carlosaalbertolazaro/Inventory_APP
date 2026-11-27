package com.example.appcrud.core.hardware.camera.domain.repository

import com.example.appcrud.core.hardware.camera.domain.model.CapturedPhoto

interface CameraRepository {
    fun createPhotoUri(): Result<CapturedPhoto>

    fun deleteTempPhoto(filePath: String): Result<Boolean>

    fun deleteOrphanedTempPhotos(retainedFilePaths: Set<String> = emptySet()): Result<Int>
}
