package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Alignment
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface AlignmentRepository {
    val allAlignmentsState: Flow<Loadable<List<Alignment>>>
}
