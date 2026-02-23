package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.BackgroundsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllBackgroundsUseCase @Inject constructor(
    private val backgroundsRepository: BackgroundsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Background>>> =
        backgroundsRepository.allBackgroundsState
}

