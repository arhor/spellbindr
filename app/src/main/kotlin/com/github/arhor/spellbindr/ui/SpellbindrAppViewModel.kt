package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.data.repository.ThemeRepository
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@Stable
@HiltViewModel
class SpellbindrAppViewModel @Inject constructor(
    private val loaders: Set<@JvmSuppressWildcards InitializableStaticAssetDataStore>,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val initialDelayPassed: Boolean = false,
        val resourcesPreloaded: Boolean = false,
        val isDarkTheme: Boolean? = null,
    ) {
        val ready: Boolean
            get() = initialDelayPassed && resourcesPreloaded
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        logger.info { "Starting application initialization" }

        viewModelScope.launch {
            observeThemeUpdates()
        }
        viewModelScope.launch {
            logger.info { "Application initialization job started" }
            awaitAll(
                async { executeInitialDelay() },
                async { executeDataLoading() },
            )
            logger.info { "Application initialization job complete" }
        }
    }

    private suspend fun observeThemeUpdates() {
        themeRepository.themeMode.collectLatest { mode ->
            val resolvedIsDark = mode?.isDark
            _state.update { it.copy(isDarkTheme = resolvedIsDark) }
        }
    }

    private suspend fun executeInitialDelay() {
        delay(1.5.seconds)
        logger.info { "Initial delay phase passed" }
        _state.update { it.copy(initialDelayPassed = true) }
    }

    private suspend fun CoroutineScope.executeDataLoading() {
        loaders.map { async { it.initialize() } }.awaitAll()
        logger.info { "Data loading phase passed" }
        _state.update { it.copy(resourcesPreloaded = true) }
    }

    companion object {
        private val logger = createLogger()
    }
}
