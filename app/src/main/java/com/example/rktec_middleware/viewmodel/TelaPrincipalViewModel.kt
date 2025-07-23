package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ExportResult {
    data class Success(val file: File) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

@HiltViewModel
class TelaPrincipalViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase, // Hilt pode injetar o DB diretamente
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult = _exportResult.asSharedFlow()

    fun exportarPlanilhaCompleta() {
        viewModelScope.launch {
            val arquivo = LogHelper.exportarPlanilhaCompleta(context, appDatabase)
            if (arquivo != null) {
                _exportResult.emit(ExportResult.Success(arquivo))
            } else {
                _exportResult.emit(ExportResult.Error("Falha ao gerar a planilha."))
            }
        }
    }

    fun promoverParaAdmin(usuario: Usuario, onFinished: () -> Unit) {
        viewModelScope.launch {
            val usuarioPromovido = usuario.copy(tipo = com.example.rktec_middleware.data.model.TipoUsuario.ADMIN)
            usuarioRepository.atualizarUsuario(usuarioPromovido)
            // A UI ainda será responsável pelo log e Toast, mas a lógica de DB fica aqui
            onFinished()
        }
    }
}