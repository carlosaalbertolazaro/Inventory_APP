package com.example.appcrud.core.hardware.audio.domain.usecase

import com.example.appcrud.core.hardware.audio.domain.model.AudioRecording
import com.example.appcrud.core.hardware.audio.domain.repository.AudioRepository
import javax.inject.Inject

class StartAudioRecordingUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
) {
    operator fun invoke(): Result<AudioRecording> = audioRepository.startRecording()
}
