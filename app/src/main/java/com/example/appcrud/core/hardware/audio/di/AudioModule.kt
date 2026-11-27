package com.example.appcrud.core.hardware.audio.di

/**
 * Hilt todavía no está habilitado en Gradle. Al integrarlo, sustituir este marcador por:
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class AudioModule {
 *     @Binds
 *     @Singleton
 *     abstract fun bindAudioRepository(impl: AudioRepositoryImpl): AudioRepository
 *
 *     companion object {
 *         @Provides
 *         @Singleton
 *         fun provideDataSource(@ApplicationContext context: Context) =
 *             AudioRecorderDataSource(context)
 *     }
 * }
 */
object AudioModule
