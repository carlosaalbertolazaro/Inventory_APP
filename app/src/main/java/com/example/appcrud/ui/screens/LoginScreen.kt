package com.example.appcrud.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.appcrud.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "IT Inventory",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Iniciar sesión")
            }
        }

        TextButton(onClick = onGoToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }

        LaunchedEffect(uiState.isLoggedIn) {
            if (uiState.isLoggedIn) {
                onLoginSuccess()
            }
        }
    }
}