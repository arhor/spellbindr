package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.FavoriteType
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Spell
import io.github.arhor.dnd.companion.domain.model.SpellDetails
import io.github.arhor.dnd.companion.domain.model.mapContent
import io.github.arhor.dnd.companion.domain.repository.FavoritesRepository
import io.github.arhor.dnd.companion.domain.repository.SpellsRepository
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
            spell.mapContent {
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
                    null -> Loadable.Failure("Spell not found.")
                    else -> Loadable.Content(spell)
                }
            } catch (e: Exception) {
                Loadable.Failure("Oops, something went wrong...", e)
            }
        )
    }
}
