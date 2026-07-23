package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAllAlignmentsUseCase @Inject constructor(
    private val alignmentRepository: AlignmentRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Alignment>>> =
        alignmentRepository.allAlignmentsState
}
