package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteSpellIdsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> = favoritesRepository.observeFavoriteIds(FavoriteType.SPELL)
}
