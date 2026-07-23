package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.usecase.ObserveSettingsUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeSettingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    init {
        observeThemeModeChanges()
    }

    fun dispatch(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> updateThemeMode(intent.mode)
        }
    }

    private fun observeThemeModeChanges() {
        observeSettingsUseCase()
            .map<AppSettings, SettingsUiState> { SettingsUiState.Content(it) }
            .onEach { state -> reduce { state } }
            .catch { throwable ->
                reduce {
                    SettingsUiState.Failure(
                        throwable.message ?: "Unable to load settings",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateThemeMode(mode: ThemeMode?) {
        val currentState = _uiState.value as? SettingsUiState.Content ?: return
        if (currentState.settings.themeMode == mode) return

        viewModelScope.launch {
            runCatching { setThemeModeUseCase(mode) }
                .onFailure { throwable ->
                    _effects.emit(
                        SettingsEffect.ShowMessage(
                            throwable.message ?: "Unable to update theme",
                        ),
                    )
                }
        }
    }

    private inline fun reduce(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
