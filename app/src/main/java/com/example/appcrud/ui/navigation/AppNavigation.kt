package com.example.appcrud.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appcrud.di.AppContainer
import com.example.appcrud.di.AppViewModelFactory
import com.example.appcrud.ui.screens.HomeScreen
import com.example.appcrud.ui.screens.LoginScreen
import com.example.appcrud.ui.screens.RegisterScreen
import com.example.appcrud.ui.viewmodel.HomeViewModel
import com.example.appcrud.ui.viewmodel.LoginViewModel
import com.example.appcrud.ui.viewmodel.RegisterViewModel

private const val LOGIN_ROUTE = "login"
private const val REGISTER_ROUTE = "register"
private const val HOME_ROUTE = "home"

@Composable
fun AppNavigation(context: Context) {
    val appContainer = remember(context) { AppContainer.getInstance(context) }
    val viewModelFactory = remember(appContainer) { AppViewModelFactory(appContainer) }
    val navController = rememberNavController()
    val startDestination = remember {
        if (appContainer.authRepository.hasActiveSession()) HOME_ROUTE else LOGIN_ROUTE
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(LOGIN_ROUTE) {
            val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = loginViewModel,
                onGoToRegister = { navController.navigate(REGISTER_ROUTE) },
                onLoginSuccess = {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(REGISTER_ROUTE) {
            val registerViewModel: RegisterViewModel = viewModel(factory = viewModelFactory)
            RegisterScreen(
                viewModel = registerViewModel,
                onGoToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HOME_ROUTE) {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onLogout = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(HOME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
