package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.common.Background
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import kotlinx.coroutines.flow.Flow
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
        get() = backgroundsDataStore.data.map { it ?: emptyList() }

    suspend fun findBackgroundById(id: String): Background? =
        allBackgrounds.firstOrNull()?.find { it.id == id }
} 
