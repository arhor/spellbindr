package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveTraitsUseCase @Inject constructor(
    private val traitsRepository: TraitsRepository,
) {
    operator fun invoke(): Flow<List<Trait>> = traitsRepository.allTraitsState.map {
        when (it) {
            is AssetState.Loading -> emptyList()
            is AssetState.Ready -> it.data
            is AssetState.Error -> emptyList()
        }
    }
}
