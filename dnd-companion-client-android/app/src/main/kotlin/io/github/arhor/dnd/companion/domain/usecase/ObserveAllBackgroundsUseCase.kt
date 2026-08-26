package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Background
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.BackgroundsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllBackgroundsUseCase @Inject constructor(
    private val backgroundsRepository: BackgroundsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Background>>> =
        backgroundsRepository.allBackgroundsState
}

