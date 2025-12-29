package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.model.AssetBootstrapState
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class DefaultAssetBootstrapper @Inject constructor(
    loaders: Set<@JvmSuppressWildcards InitializableAssetDataStore>,
) : AssetBootstrapper {

    private val logger = createLogger()
    private val criticalLoaders = loaders.filter { it.loadingPriority == AssetLoadingPriority.CRITICAL }
    private val deferredLoaders = loaders.filter { it.loadingPriority == AssetLoadingPriority.DEFERRED }
    private val started = AtomicBoolean(false)
    private val _state = MutableStateFlow(AssetBootstrapState())

    override val state: StateFlow<AssetBootstrapState>
        get() = _state.asStateFlow()

    override fun start(scope: CoroutineScope) {
        if (!started.compareAndSet(false, true)) {
            logger.info { "Asset bootstrapper is already running" }
            return
        }
        logger.info { "Asset bootstrapper started" }

        scope.launch {
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

    private suspend fun executeInitialDelay() {
        delay(1.5.seconds)
        logger.info { "Initial delay phase passed" }
        _state.update { it.copy(initialDelayPassed = true) }
    }

    private suspend fun CoroutineScope.executeCriticalDataLoading() {
        val errors = if (criticalLoaders.isNotEmpty()) {
            criticalLoaders.map { async { runCatching { it.initialize() }.exceptionOrNull() } }
                .awaitAll()
                .filterNotNull()
        } else {
            emptyList()
        }
        if (errors.isNotEmpty()) {
            val error = errors.first()
            logger.error(error) { "Critical data loading phase failed" }
            _state.update { it.copy(criticalAssetsError = error) }
        }
        logger.info { "Critical data loading phase passed" }
        _state.update { it.copy(criticalAssetsReady = true) }
    }

    private suspend fun CoroutineScope.executeDeferredDataLoading() {
        val errors = if (deferredLoaders.isNotEmpty()) {
            deferredLoaders.map { async { runCatching { it.initialize() }.exceptionOrNull() } }
                .awaitAll()
                .filterNotNull()
        } else {
            emptyList()
        }
        if (errors.isNotEmpty()) {
            val error = errors.first()
            logger.error(error) { "Deferred data loading phase failed" }
            _state.update { it.copy(deferredAssetsError = error) }
        }
        logger.info { "Deferred data loading phase passed" }
        _state.update { it.copy(deferredAssetsReady = true) }
    }
}
