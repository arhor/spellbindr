package io.github.arhor.dnd.companion.domain

import io.github.arhor.dnd.companion.domain.model.AssetBootstrapState
import kotlinx.coroutines.flow.StateFlow

interface AssetBootstrapper {
    val state: StateFlow<AssetBootstrapState>
    fun start()
    suspend fun retryFailedLoads()
}
