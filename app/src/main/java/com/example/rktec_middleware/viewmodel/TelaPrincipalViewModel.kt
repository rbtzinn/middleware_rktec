package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ExportProgress
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelaPrincipalViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportProgress>(ExportProgress.Idle)
    val exportState: StateFlow<ExportProgress> = _exportState.asStateFlow()

    fun exportarPlanilhaCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            LogHelper.exportarPlanilhaCompleta(context, appDatabase)
                .collect { progress ->
                    _exportState.value = progress
                }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportProgress.Idle
    }

    fun promoverParaAdmin(usuario: Usuario, onFinished: () -> Unit) {
        viewModelScope.launch {
            val usuarioPromovido = usuario.copy(tipo = com.example.rktec_middleware.data.model.TipoUsuario.ADMIN)
            usuarioRepository.atualizarUsuario(usuarioPromovido)
            onFinished()
        }
    }
}