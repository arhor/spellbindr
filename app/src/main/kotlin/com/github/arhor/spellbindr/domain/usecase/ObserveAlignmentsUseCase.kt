package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import com.github.arhor.spellbindr.utils.unwrap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlignmentsUseCase @Inject constructor(
    private val alignmentRepository: AlignmentRepository,
) {
    operator fun invoke(): Flow<List<Alignment>> =
        alignmentRepository
            .allAlignmentsState
            .unwrap()
}
