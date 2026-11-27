package com.example.appcrud.core.hardware.camera.data

import com.example.appcrud.core.hardware.camera.domain.model.CapturedPhoto
import com.example.appcrud.core.hardware.camera.domain.repository.CameraRepository
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val dataSource: CameraFileDataSource,
) : CameraRepository {
    override fun createPhotoUri(): Result<CapturedPhoto> = dataSource.createPhotoUri()

    override fun deleteTempPhoto(filePath: String): Result<Boolean> =
        dataSource.deleteTempPhoto(filePath)

    override fun deleteOrphanedTempPhotos(retainedFilePaths: Set<String>): Result<Int> =
        dataSource.deleteOrphanedTempPhotos(retainedFilePaths)
}
