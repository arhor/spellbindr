package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAlignmentRepository(
    initialAlignments: List<Alignment> = emptyList(),
) : AlignmentRepository {
    override val allAlignmentsState = MutableStateFlow<AssetState<List<Alignment>>>(
        AssetState.Ready(initialAlignments)
    )

    override val allAlignments: Flow<List<Alignment>> =
        allAlignmentsState.map { state ->
            when (state) {
                is AssetState.Ready -> state.data
                else -> emptyList()
            }
        }
}
