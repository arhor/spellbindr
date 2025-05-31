package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.datasource.local.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.model.Background
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundRepository @Inject constructor(
    private val backgroundsDataStore: BackgroundsAssetDataStore,
) {
    val allBackgrounds: Flow<List<Background>>
        get() = backgroundsDataStore.data.map { it ?: emptyList() }

    suspend fun findBackgroundById(id: String): Background? =
        allBackgrounds.firstOrNull()?.find { it.id == id }
} 