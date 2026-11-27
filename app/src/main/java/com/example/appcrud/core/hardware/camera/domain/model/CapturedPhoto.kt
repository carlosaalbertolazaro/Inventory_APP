package com.example.appcrud.core.hardware.camera.domain.model

import android.net.Uri

data class CapturedPhoto(
    val uri: Uri,
    val filePath: String,
)
