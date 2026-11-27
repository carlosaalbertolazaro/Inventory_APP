package com.example.appcrud.core.hardware.camera.data

import android.content.Context
import androidx.core.content.FileProvider
import com.example.appcrud.core.hardware.camera.domain.model.CapturedPhoto
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CameraFileDataSource @Inject constructor(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun createPhotoUri(): Result<CapturedPhoto> = runCatching {
        val imagesDirectory = imagesDirectory()
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}_",
            ".jpg",
            imagesDirectory,
        )
        val authority = "${appContext.packageName}.fileprovider"
        val secureUri = FileProvider.getUriForFile(appContext, authority, photoFile)

        CapturedPhoto(
            uri = secureUri,
            filePath = photoFile.absolutePath,
        )
    }

    fun deleteTempPhoto(filePath: String): Result<Boolean> = runCatching {
        val directory = imagesDirectory().canonicalFile
        val photoFile = File(filePath).canonicalFile
        require(photoFile.parentFile == directory) {
            "Solo se pueden eliminar fotografías temporales de cámara"
        }

        photoFile.exists() && photoFile.isFile && photoFile.delete()
    }

    fun deleteOrphanedTempPhotos(retainedFilePaths: Set<String>): Result<Int> = runCatching {
        val directory = imagesDirectory().canonicalFile
        val retainedPaths = retainedFilePaths.mapTo(mutableSetOf()) { path ->
            File(path).canonicalPath
        }

        directory.listFiles().orEmpty()
            .filter { file ->
                file.isFile &&
                    file.extension.equals("jpg", ignoreCase = true) &&
                    file.canonicalPath !in retainedPaths
            }
            .count { file -> file.delete() }
    }

    private fun imagesDirectory(): File {
        return File(appContext.cacheDir, "images").apply {
            if (!exists() && !mkdirs()) {
                throw IOException("No se pudo crear el directorio temporal de imágenes")
            }
        }
    }
}
