package com.example.rktec_middleware.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("usuario_logado")

object UsuarioLogadoManager {
    private val USUARIO_KEY = stringPreferencesKey("usuario_logado")

    suspend fun salvarUsuario(context: Context, nomeUsuario: String) {
        context.dataStore.edit { prefs ->
            prefs[USUARIO_KEY] = nomeUsuario
        }
    }

    suspend fun obterUsuario(context: Context): String? {
        val prefs = context.dataStore.data.first()
        return prefs[USUARIO_KEY]
    }

    suspend fun limparUsuario(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(USUARIO_KEY)
        }
    }
}
