package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.AssetState
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.dataOrNull
import com.github.arhor.spellbindr.domain.model.Background
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class BackgroundRepository @Inject constructor(
    private val backgroundsDataStore: BackgroundsAssetDataStore,
) {
    val allBackgrounds: Flow<List<Background>>
        get() = backgroundsDataStore.data.map { it.dataOrNull().orEmpty() }

    suspend fun findBackgroundById(id: String): Background? =
        backgroundsDataStore.data
            .filterIsInstance<AssetState.Ready<List<Background>>>()
            .map { it.data }
            .firstOrNull()
            ?.find { it.id == id }
}
