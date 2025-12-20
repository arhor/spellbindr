package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import javax.inject.Inject

class IsSpellFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(
        spellId: String?,
    ): Boolean = spellId?.let { favoritesRepository.isFavorite(FavoriteType.SPELL, it) } ?: false
}
