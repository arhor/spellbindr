package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.AlignmentAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Alignment
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.AlignmentRepository
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
