package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.model.AssetBootstrapState
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import com.github.arhor.spellbindr.utils.toCapitalCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAssetBootstrapper @Inject constructor(
    val applicationScope: CoroutineScope,
    val assetsDataStores: Set<@JvmSuppressWildcards AssetDataStore<*>>,
) : AssetBootstrapper {

    private val started = AtomicBoolean(false)
    private val _state = MutableStateFlow(AssetBootstrapState())

    override val state: StateFlow<AssetBootstrapState>
        get() = _state.asStateFlow()

    override fun start() {
        if (!started.compareAndSet(false, true)) {
            logger.info { "Asset bootstrapper is already running" }
            return
        }
        logger.info { "Asset bootstrapper started" }

        applicationScope.launch {
            val criticalLoadJob = launch { initializeDataStores(AssetLoadingPriority.CRITICAL) }
            val deferredLoadJob = launch { initializeDataStores(AssetLoadingPriority.DEFERRED) }

            criticalLoadJob.join()
            logger.info { "Critical initialization phase complete" }

            deferredLoadJob.join()
            logger.info { "Deferred initialization phase complete" }
        }
    }

    private suspend fun CoroutineScope.initializeDataStores(
        priority: AssetLoadingPriority
    ) {
        val errors =
            assetsDataStores.filter { it.priority == priority }
                .map { async { runCatching { it.initialize() } } }
                .awaitAll()
                .mapNotNull { it.exceptionOrNull() }

        if (errors.isNotEmpty()) {
            val error = errors.first()
            logger.error(error) { "${priority.name.toCapitalCase()} data loading phase failed" }
            onError(priority, error)
        }
        logger.info { "${priority.name.toCapitalCase()} data loading phase passed" }
        onReady(priority)
    }

    private fun onReady(priority: AssetLoadingPriority) {
        _state.update {
            when (priority) {
                AssetLoadingPriority.CRITICAL -> it.copy(criticalAssetsReady = true)
                AssetLoadingPriority.DEFERRED -> it.copy(deferredAssetsReady = true)
            }
        }
    }

    private fun onError(
        priority: AssetLoadingPriority,
        error: Throwable
    ) {
        _state.update {
            when (priority) {
                AssetLoadingPriority.CRITICAL -> it.copy(criticalAssetsError = error)
                AssetLoadingPriority.DEFERRED -> it.copy(deferredAssetsError = error)
            }
        }
    }

    companion object {
        private val logger = createLogger()
    }
}
