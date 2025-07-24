package com.example.rktec_middleware.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.rktec_middleware.data.model.ThemeOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val RFID_POWER = intPreferencesKey("rfid_power")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val VIBRATION_FEEDBACK = booleanPreferencesKey("vibration_feedback")
        // CORREÇÃO: Adicionar a definição da chave que estava faltando
        val THEME_OPTION = stringPreferencesKey("theme_option")
    }

    // --- Potência do RFID ---
    val rfIdPowerFlow: Flow<Int> = context.dataStore.data.map { it[PreferencesKeys.RFID_POWER] ?: 30 }

    suspend fun setRfIdPower(power: Int) {
        context.dataStore.edit { it[PreferencesKeys.RFID_POWER] = power }
    }

    // --- Feedback Sonoro ---
    val soundFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SOUND_FEEDBACK] ?: true }

    suspend fun setSoundFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_FEEDBACK] = enabled }
    }

    // --- Feedback de Vibração ---
    val vibrationFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.VIBRATION_FEEDBACK] ?: true }

    suspend fun setVibrationFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_FEEDBACK] = enabled }
    }

    // --- Seleção de Tema ---
    val themeOptionFlow: Flow<ThemeOption> = context.dataStore.data
        .map { preferences ->
            ThemeOption.valueOf(
                preferences[PreferencesKeys.THEME_OPTION] ?: ThemeOption.SYSTEM.name
            )
        }

    suspend fun setThemeOption(option: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_OPTION] = option.name
        }
    }
}