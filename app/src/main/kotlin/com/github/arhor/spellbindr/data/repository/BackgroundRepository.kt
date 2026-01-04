package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class BackgroundRepository @Inject constructor(
    private val backgroundsDataStore: BackgroundsAssetDataStore,
) {
    val allBackgroundsState: Flow<Loadable<List<Background>>>
        get() = backgroundsDataStore.data

    suspend fun findBackgroundById(id: String): Background? =
        when (val state = backgroundsDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Ready -> state.data.find { it.id == id }
            is Loadable.Error -> null
            is Loadable.Loading -> null
        }
}
