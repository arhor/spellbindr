package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAlignmentRepository(
    initialAlignments: List<Alignment> = emptyList(),
) : AlignmentRepository {

    override val allAlignmentsState = MutableStateFlow<AssetState<List<Alignment>>>(
        AssetState.Ready(initialAlignments)
    )
}
