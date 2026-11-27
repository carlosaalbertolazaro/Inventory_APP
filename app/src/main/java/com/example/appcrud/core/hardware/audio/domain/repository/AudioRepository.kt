package com.example.appcrud.core.hardware.audio.domain.repository

import com.example.appcrud.core.hardware.audio.domain.model.AudioRecording

interface AudioRepository {
    fun startRecording(): Result<AudioRecording>

    fun stopRecording(): Result<AudioRecording>

    fun cancelRecording(): Result<Unit>

    fun release()
}
