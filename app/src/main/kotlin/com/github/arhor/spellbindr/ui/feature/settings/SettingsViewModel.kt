package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        observeThemeMode()
            .map { SettingsUiState.Content(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun setThemeMode(mode: ThemeMode?) {
        val currState = uiState.value
        if (currState is SettingsUiState.Content && currState.themeMode != mode) {
            viewModelScope.launch {
                setThemeModeUseCase(mode)
            }
        }
    }
}
