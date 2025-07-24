package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ThemeOption
import com.example.rktec_middleware.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Expõe os fluxos do repositório como StateFlow para a UI
    val rfIdPower: StateFlow<Int> = settingsRepository.rfIdPowerFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val soundFeedbackEnabled: StateFlow<Boolean> = settingsRepository.soundFeedbackFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationFeedbackEnabled: StateFlow<Boolean> = settingsRepository.vibrationFeedbackFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateRfIdPower(power: Int) {
        viewModelScope.launch {
            settingsRepository.setRfIdPower(power)
        }
    }

    fun updateSoundFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundFeedback(enabled)
        }
    }

    fun updateVibrationFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationFeedback(enabled)
        }
    }

    val themeOption: StateFlow<ThemeOption> = settingsRepository.themeOptionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeOption.SYSTEM)

    fun updateThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            settingsRepository.setThemeOption(option)
        }
    }
}