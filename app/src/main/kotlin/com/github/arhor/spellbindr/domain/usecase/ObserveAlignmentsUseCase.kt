package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAlignmentsUseCase @Inject constructor(
    private val alignmentRepository: AlignmentRepository,
) {
    operator fun invoke(): Flow<List<Alignment>> = alignmentRepository.allAlignmentsState.map {
        when (it) {
            is AssetState.Loading -> emptyList()
            is AssetState.Ready -> it.data
            is AssetState.Error -> emptyList()
        }
    }
}
