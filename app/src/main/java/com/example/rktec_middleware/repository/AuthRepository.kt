package com.example.rktec_middleware.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    // Retorna o usuário atualmente logado no Firebase
    fun getCurrentUser() = auth.currentUser

    /**
     * Envia um e-mail de redefinição de senha para o endereço de e-mail fornecido.
     * @return Retorna 'true' se o e-mail foi enviado com sucesso, 'false' caso contrário.
     */
    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            // É uma boa prática registrar o erro para depuração
            // Log.e("AuthRepository", "Erro ao enviar e-mail de redefinição", e)
            false
        }
    }

    /**
     * Desloga o usuário atual do Firebase.
     */
    fun logout() {
        auth.signOut()
    }

    // Adicione outras funções de autenticação que você precisar aqui...
}
