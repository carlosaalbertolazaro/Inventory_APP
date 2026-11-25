package com.example.appcrud.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appcrud.ui.viewmodel.HomeViewModel
import com.example.appcrud.ui.viewmodel.LoginViewModel
import com.example.appcrud.ui.viewmodel.RegisterViewModel

class AppViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(appContainer.authRepository) as T
            RegisterViewModel::class.java -> RegisterViewModel(appContainer.authRepository) as T
            HomeViewModel::class.java -> HomeViewModel(
                appContainer.equipoRepository,
                appContainer.authRepository
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
