package com.example.appcrud.core.hardware.audio.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.appcrud.core.hardware.audio.domain.model.AudioRecording
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AudioRecorderDataSource internal constructor(
    context: Context,
    private val recorderFactory: MediaRecorderFactory,
) {
    @Inject
    constructor(context: Context) : this(context, DefaultMediaRecorderFactory)

    private val appContext = context.applicationContext
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    @Synchronized
    fun startRecording(): Result<AudioRecording> {
        if (mediaRecorder != null) {
            val abandonedFile = currentFile
            release()
            abandonedFile?.delete()
            return Result.failure(IllegalStateException("Ya hay una grabación en curso"))
        }

        var outputFile: File? = null
        var started = false
        return try {
            val file = createRecordingFile().also {
                outputFile = it
                currentFile = it
            }
            val recorder = recorderFactory.create(appContext).also { mediaRecorder = it }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(AUDIO_BIT_RATE)
                setAudioSamplingRate(AUDIO_SAMPLE_RATE)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            started = true
            Result.success(AudioRecording(file.absolutePath))
        } catch (error: Exception) {
            outputFile?.delete()
            Result.failure(error)
        } finally {
            if (!started) release()
        }
    }

    @Synchronized
    fun stopRecording(): Result<AudioRecording> {
        val recorder = mediaRecorder
        val file = currentFile
        if (recorder == null || file == null) {
            release()
            return Result.failure(IllegalStateException("No hay una grabación en curso"))
        }

        return try {
            recorder.stop()
            Result.success(AudioRecording(file.absolutePath))
        } catch (error: Exception) {
            file.delete()
            Result.failure(error)
        } finally {
            release()
        }
    }

    @Synchronized
    fun cancelRecording(): Result<Unit> {
        val recorder = mediaRecorder
        val file = currentFile
        if (recorder == null) {
            release()
            return Result.success(Unit)
        }

        return try {
            recorder.stop()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            file?.delete()
            release()
        }
    }

    @Synchronized
    fun release() {
        val recorder = mediaRecorder
        try {
            recorder?.release()
        } catch (_: Exception) {
            // La liberación es idempotente y no debe provocar un cierre de la app.
        } finally {
            mediaRecorder = null
            currentFile = null
        }
    }

    private fun createRecordingFile(): File {
        val audioDirectory = File(appContext.cacheDir, "audio").apply {
            if (!exists() && !mkdirs()) {
                throw IOException("No se pudo crear el directorio temporal de audio")
            }
        }
        return File.createTempFile(
            "AUDIO_${System.currentTimeMillis()}_",
            ".m4a",
            audioDirectory,
        )
    }

    private companion object {
        const val AUDIO_BIT_RATE = 128_000
        const val AUDIO_SAMPLE_RATE = 44_100
    }
}

internal fun interface MediaRecorderFactory {
    fun create(context: Context): MediaRecorder
}

private object DefaultMediaRecorderFactory : MediaRecorderFactory {
    @Suppress("DEPRECATION")
    override fun create(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
}
