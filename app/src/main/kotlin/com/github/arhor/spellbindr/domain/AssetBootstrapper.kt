package com.github.arhor.spellbindr.domain

import com.github.arhor.spellbindr.domain.model.AssetBootstrapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface AssetBootstrapper {
    val state: StateFlow<AssetBootstrapState>
    fun start(scope: CoroutineScope)
}
