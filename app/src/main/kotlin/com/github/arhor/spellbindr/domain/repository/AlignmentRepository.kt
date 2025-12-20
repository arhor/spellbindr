package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import kotlinx.coroutines.flow.Flow

interface AlignmentRepository {
    val allAlignments: Flow<List<Alignment>>
}
