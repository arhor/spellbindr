package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteSpellIdsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    operator fun invoke(): Flow<List<String>> = spellsRepository.favoriteSpellIds
}
