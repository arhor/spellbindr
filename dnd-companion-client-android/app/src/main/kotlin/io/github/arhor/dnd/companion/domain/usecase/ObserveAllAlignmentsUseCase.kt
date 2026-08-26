package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Alignment
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.AlignmentRepository
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
