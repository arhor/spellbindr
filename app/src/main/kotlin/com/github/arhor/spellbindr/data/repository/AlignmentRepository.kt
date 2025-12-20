package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository as AlignmentRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlignmentRepository @Inject constructor(
    private val alignmentDataStore: AlignmentAssetDataStore,
) : AlignmentRepositoryContract {
    override val allAlignments: Flow<List<Alignment>>
        get() = alignmentDataStore.data.map { alignments -> alignments.orEmpty().map { it.toDomain() } }
}
