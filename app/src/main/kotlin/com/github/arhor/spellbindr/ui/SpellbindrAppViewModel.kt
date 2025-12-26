package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.local.assets.AssetLoadingPriority
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
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

    private val criticalLoaders = loaders.filter { it.loadingPriority == AssetLoadingPriority.CRITICAL }
    private val deferredLoaders = loaders.filter { it.loadingPriority == AssetLoadingPriority.DEFERRED }

    @Immutable
    data class State(
        val initialDelayPassed: Boolean = false,
        val criticalAssetsReady: Boolean = false,
        val deferredAssetsReady: Boolean = false,
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
            logger.info { "Application initialization job started" }

            val deferredLoadJob = launch { executeDeferredDataLoading() }
            awaitAll(
                async { executeInitialDelay() },
                async { executeCriticalDataLoading() },
            )
            logger.info { "Critical initialization phase complete" }

            deferredLoadJob.join()

            logger.info { "Deferred initialization phase complete" }
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

    private suspend fun CoroutineScope.executeCriticalDataLoading() {
        if (criticalLoaders.isNotEmpty()) {
            criticalLoaders.map { async { it.initialize() } }.awaitAll()
        }
        logger.info { "Critical data loading phase passed" }
        _state.update { it.copy(criticalAssetsReady = true) }
    }

    private suspend fun CoroutineScope.executeDeferredDataLoading() {
        if (deferredLoaders.isNotEmpty()) {
            deferredLoaders.map { async { it.initialize() } }.awaitAll()
        }
        logger.info { "Deferred data loading phase passed" }
        _state.update { it.copy(deferredAssetsReady = true) }
    }

    companion object {
        private val logger = createLogger()
    }
}
