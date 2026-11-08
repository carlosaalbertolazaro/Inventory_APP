package com.example.appcrud.di

import android.content.Context
import com.example.appcrud.data.local.SessionStorage
import com.example.appcrud.data.repository.EquipoRepositoryImpl
import com.example.appcrud.data.remote.ApiService
import com.example.appcrud.data.remote.ApiServiceFactory

class AppContainer private constructor(context: Context) {

    val sessionStorage: SessionStorage = SessionStorage.getInstance(context.applicationContext)
    val apiServiceFactory: ApiServiceFactory = ApiServiceFactory.create(context.applicationContext)
    val apiService: ApiService = apiServiceFactory.create()
    val equipoRepository: EquipoRepositoryImpl = EquipoRepositoryImpl(apiService = apiService)

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