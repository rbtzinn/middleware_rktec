package com.example.rktec_middleware.util

import android.content.Context
import android.util.Log

object UsuarioLogadoManager {
    private const val PREFS_NAME = "usuario_logado"
    private const val CHAVE_USUARIO = "email"

    fun salvarUsuario(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(CHAVE_USUARIO, email).apply()
        Log.d("RKTEC", "SALVOU EMAIL NAS PREFS: $email")
    }

    fun obterUsuario(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(CHAVE_USUARIO, null)
        Log.d("RKTEC", "PEGOU EMAIL DAS PREFS: $email")
        return email
    }

    fun limparUsuario(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(CHAVE_USUARIO).apply()
        Log.d("RKTEC", "LIMPOU EMAIL DAS PREFS")
    }
}
