package com.example.appcrud.di

import android.content.Context
import com.example.appcrud.data.local.SessionStorage
import com.example.appcrud.data.remote.ApiServiceFactory
import com.example.appcrud.data.repository.AuthRepositoryImpl
import com.example.appcrud.data.repository.EquipoRepositoryImpl
import com.example.appcrud.domain.repository.AuthRepository
import com.example.appcrud.domain.repository.EquipoRepository

class AppContainer private constructor(context: Context) {

    private val sessionStorage: SessionStorage =
        SessionStorage.getInstance(context.applicationContext)
    private val apiService = ApiServiceFactory.create(context.applicationContext).create()

    val authRepository: AuthRepository = AuthRepositoryImpl(apiService, sessionStorage)
    val equipoRepository: EquipoRepository = EquipoRepositoryImpl(apiService)

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return instance ?: synchronized(this) {
                instance ?: AppContainer(context).also { instance = it }
            }
        }
    }
}
