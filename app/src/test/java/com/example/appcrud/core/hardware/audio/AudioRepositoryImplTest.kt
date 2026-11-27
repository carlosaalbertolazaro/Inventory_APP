package com.example.appcrud.core.hardware.audio

import android.content.Context
import android.media.MediaRecorder
import com.example.appcrud.core.hardware.audio.data.AudioRecorderDataSource
import com.example.appcrud.core.hardware.audio.data.AudioRepositoryImpl
import com.example.appcrud.core.hardware.audio.data.MediaRecorderFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class AudioRepositoryImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var recorder: MediaRecorder
    private lateinit var repository: AudioRepositoryImpl

    @Before
    fun setUp() {
        val context = mockk<Context>()
        recorder = mockk(relaxed = true)
        every { context.applicationContext } returns context
        every { context.cacheDir } returns temporaryFolder.root
        val dataSource = AudioRecorderDataSource(
            context,
            MediaRecorderFactory { recorder },
        )
        repository = AudioRepositoryImpl(dataSource)
    }

    @Test
    fun `ciclo start stop release crea m4a y libera una sola vez`() {
        val startResult = repository.startRecording()

        assertTrue(startResult.isSuccess)
        val audioFile = File(startResult.getOrThrow().filePath)
        assertTrue(audioFile.isFile)
        assertEquals("m4a", audioFile.extension)

        val stopResult = repository.stopRecording()
        repository.release()

        assertTrue(stopResult.isSuccess)
        assertEquals(audioFile.absolutePath, stopResult.getOrThrow().filePath)
        verifyOrder {
            recorder.prepare()
            recorder.start()
            recorder.stop()
            recorder.release()
        }
        verify(exactly = 1) { recorder.release() }
    }

    @Test
    fun `detener sin grabacion activa devuelve estado invalido`() {
        val result = repository.stopRecording()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("No hay una grabación en curso", result.exceptionOrNull()?.message)
        verify(exactly = 0) { recorder.stop() }
    }

    @Test
    fun `fallo al preparar libera el recorder y permite detectar el estado limpio`() {
        every { recorder.prepare() } throws IOException("No se pudo preparar")

        val startResult = repository.startRecording()
        val stopResult = repository.stopRecording()

        assertTrue(startResult.isFailure)
        assertTrue(startResult.exceptionOrNull() is IOException)
        assertTrue(stopResult.exceptionOrNull() is IllegalStateException)
        verify(exactly = 1) { recorder.release() }
        verify(exactly = 0) { recorder.stop() }
    }

    @Test
    fun `fallo al detener libera y elimina el archivo incompleto`() {
        val audioFile = File(repository.startRecording().getOrThrow().filePath)
        every { recorder.stop() } throws IllegalStateException("Estado inválido")

        val result = repository.stopRecording()

        assertTrue(result.isFailure)
        assertFalse(audioFile.exists())
        verify(exactly = 1) { recorder.release() }
        assertTrue(repository.stopRecording().exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `cancelar detiene libera y elimina el audio temporal`() {
        val audioFile = File(repository.startRecording().getOrThrow().filePath)

        val result = repository.cancelRecording()

        assertTrue(result.isSuccess)
        assertFalse(audioFile.exists())
        verify(exactly = 1) { recorder.stop() }
        verify(exactly = 1) { recorder.release() }
    }
}
