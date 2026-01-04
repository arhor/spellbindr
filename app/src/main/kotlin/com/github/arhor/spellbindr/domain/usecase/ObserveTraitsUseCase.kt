package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTraitsUseCase @Inject constructor(
    private val traitsRepository: TraitsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Trait>>> =
        traitsRepository.allTraitsState
}
