package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.ThemePreference
import com.github.arhor.spellbindr.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    data class SettingsViewState(
        val themePreference: ThemePreference = ThemePreference.SYSTEM
    )

    val state: StateFlow<SettingsViewState> =
        settingsRepository.themePreferenceFlow
            .map { SettingsViewState(themePreference = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsViewState()
            )

    fun setThemePreference(themePreference: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(themePreference)
        }
    }
} 