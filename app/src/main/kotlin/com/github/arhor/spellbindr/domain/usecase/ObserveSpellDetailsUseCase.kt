package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellDetails
import com.github.arhor.spellbindr.domain.model.map
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveSpellDetailsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(spellId: String): Flow<Loadable<SpellDetails>> =
        combine(
            getSpellByIdFlow(spellId),
            favoritesRepository.observeFavoriteIds(FavoriteType.SPELL),
        ) { spell, favorites ->
            spell.map {
                SpellDetails(
                    spell = it,
                    isFavorite = it.id in favorites
                )
            }
        }

    private fun getSpellByIdFlow(spellId: String): Flow<Loadable<Spell>> = flow {
        emit(value = Loadable.Loading)
        emit(
            value = try {
                when (val spell = spellsRepository.getSpellById(spellId)) {
                    null -> Loadable.Error("Spell not found.")
                    else -> Loadable.Ready(spell)
                }
            } catch (e: Exception) {
                Loadable.Error("Oops, something went wrong...", e)
            }
        )
    }
}
