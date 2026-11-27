package com.example.appcrud.core.hardware.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.appcrud.core.hardware.camera.data.CameraFileDataSource
import com.example.appcrud.core.hardware.camera.data.CameraRepositoryImpl
import com.example.appcrud.core.hardware.camera.domain.model.CapturedPhoto
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CameraRepositoryImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: CameraRepositoryImpl
    private lateinit var secureUri: Uri

    @Before
    fun setUp() {
        context = mockk()
        secureUri = mockk()
        every { context.applicationContext } returns context
        every { context.cacheDir } returns temporaryFolder.root
        every { context.packageName } returns PACKAGE_NAME
        mockkStatic(FileProvider::class)
        every {
            FileProvider.getUriForFile(context, AUTHORITY, any())
        } returns secureUri

        repository = CameraRepositoryImpl(CameraFileDataSource(context))
    }

    @After
    fun tearDown() {
        unmockkStatic(FileProvider::class)
    }

    @Test
    fun `crea un archivo jpg temporal y devuelve su uri segura`() {
        val result = repository.createPhotoUri()

        assertTrue(result.isSuccess)
        val photo = result.getOrThrow()
        val photoFile = File(photo.filePath)
        assertSame(secureUri, photo.uri)
        assertTrue(photoFile.isFile)
        assertEquals("jpg", photoFile.extension)
        assertEquals(
            File(temporaryFolder.root, "images").canonicalFile,
            photoFile.parentFile?.canonicalFile,
        )
        verify(exactly = 1) {
            FileProvider.getUriForFile(
                context,
                AUTHORITY,
                match { file -> file.absolutePath == photo.filePath },
            )
        }
    }

    @Test
    fun `elimina el archivo temporal asociado a la fotografia`() {
        val photo = createPhoto()

        val result = repository.deleteTempPhoto(photo.filePath)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertFalse(File(photo.filePath).exists())
    }

    @Test
    fun `limpia huerfanos sin borrar las rutas conservadas`() {
        val imagesDirectory = temporaryFolder.newFolder("images")
        val retained = File(imagesDirectory, "retained.jpg").apply { writeText("keep") }
        val orphan = File(imagesDirectory, "orphan.jpg").apply { writeText("delete") }

        val result = repository.deleteOrphanedTempPhotos(setOf(retained.absolutePath))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())
        assertTrue(retained.exists())
        assertFalse(orphan.exists())
    }

    private fun createPhoto(): CapturedPhoto = repository.createPhotoUri().getOrThrow()

    private companion object {
        const val PACKAGE_NAME = "com.example.appcrud"
        const val AUTHORITY = "$PACKAGE_NAME.fileprovider"
    }
}
