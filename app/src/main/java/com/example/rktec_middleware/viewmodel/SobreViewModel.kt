package com.example.rktec_middleware.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class para organizar as informações de diagnóstico
data class InfoDiagnostico(
    val versaoApp: String = "",
    val usuarioEmail: String = "",
    val usuarioTipo: String = "",
    val companyId: String = ""
)

@HiltViewModel
class SobreViewModel @Inject constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _infoDiagnostico = MutableStateFlow(InfoDiagnostico())
    val infoDiagnostico = _infoDiagnostico.asStateFlow()

    init {
        carregarInformacoes()
    }

    private fun carregarInformacoes() {
        viewModelScope.launch {
            val email = firebaseAuth.currentUser?.email
            val usuario = email?.let { usuarioRepository.buscarPorEmail(it) }

            // Pegando versão diretamente do PackageManager
            val context = getApplication<Application>()
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versao = "${packageInfo.versionName} (${packageInfo.longVersionCode})"

            _infoDiagnostico.value = InfoDiagnostico(
                versaoApp = versao,
                usuarioEmail = usuario?.email ?: "Não logado",
                usuarioTipo = usuario?.tipo?.name ?: "N/A",
                companyId = usuario?.companyId ?: "N/A"
            )
        }
    }
}
