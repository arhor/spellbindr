package com.github.arhor.spellbindr.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeSettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.themeMode.collect { mode ->
                _state.update { current ->
                    current.copy(
                        themeMode = mode,
                        loaded = true,
                    )
                }
            }
        }
    }

    fun onThemeModeSelected(mode: ThemeMode?) {
        setThemeMode(mode)
    }

    private fun setThemeMode(mode: ThemeMode?) {
        if (state.value.themeMode == mode) return
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }
}
