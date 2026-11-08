package com.example.appcrud

import android.app.Application
import com.example.appcrud.di.AppContainer

class MainApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer.getInstance(this)
    }
}
