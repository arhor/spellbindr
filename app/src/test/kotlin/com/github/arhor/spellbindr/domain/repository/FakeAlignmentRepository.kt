package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAlignmentRepository(
    initialAlignments: List<Alignment> = emptyList(),
) : AlignmentRepository {
    val allAlignmentsState = MutableStateFlow(initialAlignments)

    override val allAlignments: StateFlow<List<Alignment>> = allAlignmentsState
}
