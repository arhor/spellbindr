package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.usecase.ObserveSettingsUseCase
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellbindrAppViewModel @Inject constructor(
    private val assetBootstrapper: AssetBootstrapper,
    private val observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    @Immutable
    data class State(
        val criticalAssetsReady: Boolean = false,
        val deferredAssetsReady: Boolean = false,
        val criticalAssetsError: Throwable? = null,
        val deferredAssetsError: Throwable? = null,
        val isDarkTheme: Boolean? = null,
    ) {
        val readyForInteraction: Boolean
            get() = criticalAssetsReady

        val fullyReady: Boolean
            get() = criticalAssetsReady && deferredAssetsReady
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        logger.info { "Starting application initialization" }

        viewModelScope.launch {
            observeThemeUpdates()
        }
        viewModelScope.launch {
            observeBootstrapperState()
        }
    }

    private suspend fun observeThemeUpdates() {
        observeSettings().collectLatest {
            _state.update { state ->
                state.copy(
                    isDarkTheme = it.themeMode?.isDark,
                )
            }
        }
    }

    private suspend fun observeBootstrapperState() {
        assetBootstrapper.state.collectLatest {
            _state.update { state ->
                state.copy(
                    criticalAssetsReady = it.criticalAssetsReady,
                    deferredAssetsReady = it.deferredAssetsReady,
                    criticalAssetsError = it.criticalAssetsError,
                    deferredAssetsError = it.deferredAssetsError,
                )
            }
        }
    }

    companion object {
        private val logger = createLogger()
    }
}
