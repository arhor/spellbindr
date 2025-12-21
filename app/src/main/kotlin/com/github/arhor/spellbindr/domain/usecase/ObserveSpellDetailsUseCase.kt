package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveSpellDetailsUseCase @Inject constructor(
    private val getSpellByIdUseCase: GetSpellByIdUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
) {
    operator fun invoke(spellId: String): Flow<SpellDetailsState> {
        val spellResultFlow: Flow<SpellResult> = flow {
            emit(SpellResult.Loading)
            runCatching { getSpellByIdUseCase(spellId) }
                .onSuccess { spell ->
                    if (spell == null) {
                        emit(SpellResult.Error("Spell not found."))
                    } else {
                        emit(SpellResult.Loaded(spell))
                    }
                }
                .onFailure {
                    emit(SpellResult.Error("Oops, something went wrong..."))
                }
        }

        return combine(spellResultFlow, observeFavoriteSpellIdsUseCase()) { spellResult, favoriteIds ->
            when (spellResult) {
                SpellResult.Loading -> SpellDetailsState.Loading
                is SpellResult.Error -> SpellDetailsState.Error(spellResult.message)
                is SpellResult.Loaded -> SpellDetailsState.Loaded(
                    spell = spellResult.spell,
                    isFavorite = favoriteIds.contains(spellResult.spell.id),
                )
            }
        }
    }

    sealed interface SpellDetailsState {
        data object Loading : SpellDetailsState

        data class Loaded(
            val spell: Spell,
            val isFavorite: Boolean,
        ) : SpellDetailsState

        data class Error(val message: String) : SpellDetailsState
    }

    private sealed interface SpellResult {
        data object Loading : SpellResult

        data class Loaded(val spell: Spell) : SpellResult

        data class Error(val message: String) : SpellResult
    }
}
