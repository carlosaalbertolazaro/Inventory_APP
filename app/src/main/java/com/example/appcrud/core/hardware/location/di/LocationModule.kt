package com.example.appcrud.core.hardware.location.di

/**
 * Hilt todavía no está habilitado en Gradle. Al integrarlo, sustituir este marcador por:
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class LocationModule {
 *     @Binds
 *     @Singleton
 *     abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
 *
 *     companion object {
 *         @Provides
 *         @Singleton
 *         fun provideFusedClient(@ApplicationContext context: Context) =
 *             LocationServices.getFusedLocationProviderClient(context)
 *
 *         @Provides
 *         @Singleton
 *         fun provideDataSource(
 *             @ApplicationContext context: Context,
 *             client: FusedLocationProviderClient,
 *         ) = LocationDataSource(context, client)
 *     }
 * }
 */
object LocationModule
