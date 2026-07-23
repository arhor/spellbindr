package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteSpellUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(spellId: String) {
        favoritesRepository.toggleFavorite(FavoriteType.SPELL, spellId)
    }
}
