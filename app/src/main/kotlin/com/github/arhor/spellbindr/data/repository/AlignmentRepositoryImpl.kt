package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.dataOrNull
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlignmentRepositoryImpl @Inject constructor(
    private val alignmentDataStore: AlignmentAssetDataStore,
) : AlignmentRepository {
    override val allAlignments: Flow<List<Alignment>>
        get() = alignmentDataStore.data.map { it.dataOrNull().orEmpty() }
}
