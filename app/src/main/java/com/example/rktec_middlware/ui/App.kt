package com.example.rktec_middleware.ui

import androidx.compose.runtime.Composable
import com.example.rktec_middleware.ui.screens.TelaPrincipalScreen
import com.example.rktec_middleware.viewmodel.RfidViewModel

@Composable
fun App(viewModel: RfidViewModel) {
    TelaPrincipalScreen(viewModel = viewModel)
}
