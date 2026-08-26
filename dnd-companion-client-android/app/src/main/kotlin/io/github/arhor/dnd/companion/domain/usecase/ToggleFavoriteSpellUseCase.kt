package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.FavoriteType
import io.github.arhor.dnd.companion.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteSpellUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(spellId: String) {
        favoritesRepository.toggleFavorite(FavoriteType.SPELL, spellId)
    }
}
