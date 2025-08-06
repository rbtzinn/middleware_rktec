package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CadastroState {
    object Idle : CadastroState()
    object Loading : CadastroState()
    object Sucesso : CadastroState()
    data class Erro(val mensagem: String) : CadastroState()
}

@HiltViewModel
class CadastroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    private val _cadastroState = MutableStateFlow<CadastroState>(CadastroState.Idle)
    val cadastroState: StateFlow<CadastroState> = _cadastroState.asStateFlow()

    // MUDANÇA PRINCIPAL: A função agora recebe e valida o código de convite.
    // Em CadastroViewModel.kt

    // Em CadastroViewModel.kt

    fun cadastrar(nome: String, email: String, senha: String, codigoConvite: String) {
        _cadastroState.value = CadastroState.Loading
        // AQUI ESTÁ A CORREÇÃO: .trim() remove espaços e .uppercase() converte para maiúsculas
        val codigoFormatado = codigoConvite.trim().uppercase()

        // Passo 1: Valida o código da empresa no Firestore
        Firebase.firestore.collection("empresas")
            .whereEqualTo("codigoConvite", codigoFormatado) // Usando o código formatado
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Se a busca não retornar nada, o código é inválido
                    _cadastroState.value = CadastroState.Erro("Código da empresa inválido!")
                    return@addOnSuccessListener
                }

                // Pega o ID do documento da empresa encontrada
                val companyId = querySnapshot.documents.first().id

                // Passo 2: Se o código for válido, prossegue com o cadastro do usuário no Firebase Auth
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener { result ->
                        val user = result.user

                        user?.sendEmailVerification()
                        user?.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName(nome).build()
                        )?.addOnCompleteListener {
                            // Passo 3: Salva o usuário no nosso banco de dados (Room e Firestore)
                            viewModelScope.launch {
                                try {
                                    val usuario = Usuario(
                                        nome = nome,
                                        email = email,
                                        senhaHash = "", // Não é necessário guardar o hash aqui
                                        tipo = TipoUsuario.MEMBRO,
                                        ativo = true,
                                        companyId = companyId // Salva o novo usuário com o ID da empresa
                                    )
                                    // Esta função já salva tanto no Room quanto no Firestore
                                    usuarioRepository.cadastrarUsuario(usuario)
                                    _cadastroState.value = CadastroState.Sucesso
                                } catch (e: Exception) {
                                    _cadastroState.value = CadastroState.Erro("Erro ao salvar dados: ${e.message}")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _cadastroState.value = CadastroState.Erro(e.message ?: "E-mail já está em uso ou é inválido.")
                    }
            }
            .addOnFailureListener { e ->
                _cadastroState.value = CadastroState.Erro("Erro ao verificar empresa: ${e.message}")
            }
    }

    fun resetar() {
        _cadastroState.value = CadastroState.Idle
    }
}