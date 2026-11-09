package com.example.appcrud.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcrud.di.AppContainer
import com.example.appcrud.di.AppViewModelFactory
import com.example.appcrud.ui.screens.HomeScreen
import com.example.appcrud.ui.screens.LoginScreen
import com.example.appcrud.ui.screens.RegisterScreen

@Composable
fun AppNavigation(context: Context) {
    val appContainer = AppContainer.getInstance(context)
    val viewModelFactory = AppViewModelFactory(appContainer)
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val loginViewModel: com.example.appcrud.ui.viewmodel.LoginViewModel =
                viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = loginViewModel,
                onGoToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") }
            )
        }
        composable("register") {
            val registerViewModel: com.example.appcrud.ui.viewmodel.RegisterViewModel =
                viewModel(factory = viewModelFactory)
            RegisterScreen(
                viewModel = registerViewModel,
                onGoToLogin = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }
        composable("home") {
            val homeViewModel: com.example.appcrud.ui.viewmodel.HomeViewModel =
                viewModel(factory = viewModelFactory)
            HomeScreen(viewModel = homeViewModel)
        }
    }
}