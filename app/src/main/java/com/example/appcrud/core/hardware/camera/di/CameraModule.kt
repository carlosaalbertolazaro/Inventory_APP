package com.example.appcrud.core.hardware.camera.di

/**
 * Hilt todavía no está habilitado en Gradle. Al integrarlo, sustituir este marcador por:
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class CameraModule {
 *     @Binds
 *     @Singleton
 *     abstract fun bindCameraRepository(impl: CameraRepositoryImpl): CameraRepository
 *
 *     companion object {
 *         @Provides
 *         @Singleton
 *         fun provideDataSource(@ApplicationContext context: Context) =
 *             CameraFileDataSource(context)
 *     }
 * }
 */
object CameraModule
