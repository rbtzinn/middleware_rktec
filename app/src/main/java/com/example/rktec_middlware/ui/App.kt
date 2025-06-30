package com.example.rktec_middleware.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.ui.screens.TelaPrincipalScreen

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tela_principal") {
        composable("tela_principal") {
            TelaPrincipalScreen()
        }
    }
}
