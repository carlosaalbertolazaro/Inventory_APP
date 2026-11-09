package com.example.appcrud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appcrud.ui.navigation.AppNavigation
import com.example.appcrud.ui.theme.AppCrudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCrudTheme {
                AppNavigation(context = this)
            }
        }
    }
}