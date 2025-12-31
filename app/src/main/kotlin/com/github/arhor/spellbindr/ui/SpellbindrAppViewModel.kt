package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
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
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val initialDelayPassed: Boolean = false,
        val criticalAssetsReady: Boolean = false,
        val deferredAssetsReady: Boolean = false,
        val criticalAssetsError: Throwable? = null,
        val deferredAssetsError: Throwable? = null,
        val isDarkTheme: Boolean? = null,
    ) {
        val readyForInteraction: Boolean
            get() = initialDelayPassed && criticalAssetsReady

        val fullyReady: Boolean
            get() = readyForInteraction && deferredAssetsReady
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
        themeRepository.themeMode.collectLatest { mode ->
            val resolvedIsDark = mode?.isDark
            _state.update { it.copy(isDarkTheme = resolvedIsDark) }
        }
    }

    private suspend fun observeBootstrapperState() {
        assetBootstrapper.state.collectLatest { bootstrapState ->
            _state.update {
                it.copy(
                    initialDelayPassed = bootstrapState.initialDelayPassed,
                    criticalAssetsReady = bootstrapState.criticalAssetsReady,
                    deferredAssetsReady = bootstrapState.deferredAssetsReady,
                    criticalAssetsError = bootstrapState.criticalAssetsError,
                    deferredAssetsError = bootstrapState.deferredAssetsError,
                )
            }
        }
    }

    companion object {
        private val logger = createLogger()
    }
}
