package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface AlignmentRepository {
    val allAlignmentsState: Flow<Loadable<List<Alignment>>>
}
