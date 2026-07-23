package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.ConditionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAllConditionsUseCase @Inject constructor(
    private val conditionsRepository: ConditionsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Condition>>> =
        conditionsRepository.allConditionsState
}
