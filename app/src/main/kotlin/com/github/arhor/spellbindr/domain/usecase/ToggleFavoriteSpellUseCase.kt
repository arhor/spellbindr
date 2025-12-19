package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import javax.inject.Inject

class ToggleFavoriteSpellUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    suspend operator fun invoke(spellId: String) {
        spellsRepository.toggleFavorite(spellId)
    }
}
