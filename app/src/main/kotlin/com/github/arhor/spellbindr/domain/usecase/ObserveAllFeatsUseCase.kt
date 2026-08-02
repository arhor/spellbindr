package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.FeatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllFeatsUseCase @Inject constructor(
    private val featsRepository: FeatsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Feat>>> = featsRepository.allFeatsState
}
