package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import com.github.arhor.spellbindr.utils.unwrap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTraitsUseCase @Inject constructor(
    private val traitsRepository: TraitsRepository,
) {
    operator fun invoke(): Flow<List<Trait>> =
        traitsRepository
            .allTraitsState
            .unwrap()
}
