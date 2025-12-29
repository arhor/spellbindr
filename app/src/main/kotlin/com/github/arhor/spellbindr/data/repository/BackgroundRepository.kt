package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Background
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class BackgroundRepository @Inject constructor(
    private val backgroundsDataStore: BackgroundsAssetDataStore,
) {
    val allBackgroundsState: Flow<AssetState<List<Background>>>
        get() = backgroundsDataStore.data

    suspend fun findBackgroundById(id: String): Background? =
        when (val state = backgroundsDataStore.data.first { it !is AssetState.Loading }) {
            is AssetState.Ready -> state.data.find { it.id == id }
            is AssetState.Error -> null
            is AssetState.Loading -> null
        }
}
