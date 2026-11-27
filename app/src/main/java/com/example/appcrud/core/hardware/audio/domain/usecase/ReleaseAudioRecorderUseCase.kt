package com.example.appcrud.core.hardware.audio.domain.usecase

import com.example.appcrud.core.hardware.audio.domain.repository.AudioRepository
import javax.inject.Inject

class ReleaseAudioRecorderUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
) {
    operator fun invoke() = audioRepository.release()
}
