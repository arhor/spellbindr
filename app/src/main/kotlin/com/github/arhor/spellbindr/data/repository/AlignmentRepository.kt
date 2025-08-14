package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.model.Alignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlignmentRepository @Inject constructor(
    private val alignmentDataStore: AlignmentAssetDataStore,
) {
    val allAlignments: Flow<List<Alignment>>
        get() = alignmentDataStore.data.map { it ?: emptyList() }
}