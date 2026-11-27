package com.example.appcrud.core.hardware.audio.data

import com.example.appcrud.core.hardware.audio.domain.model.AudioRecording
import com.example.appcrud.core.hardware.audio.domain.repository.AudioRepository
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val dataSource: AudioRecorderDataSource,
) : AudioRepository {
    override fun startRecording(): Result<AudioRecording> = dataSource.startRecording()

    override fun stopRecording(): Result<AudioRecording> = try {
        dataSource.stopRecording()
    } finally {
        dataSource.release()
    }

    override fun cancelRecording(): Result<Unit> = try {
        dataSource.cancelRecording()
    } finally {
        dataSource.release()
    }

    override fun release() = dataSource.release()
}
