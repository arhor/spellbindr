package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlignmentRepositoryImpl @Inject constructor(
    private val alignmentDataStore: AlignmentAssetDataStore,
) : AlignmentRepository {

    override val allAlignmentsState: Flow<Loadable<List<Alignment>>>
        get() = alignmentDataStore.data
}
