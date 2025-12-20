package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellDetailsViewModel @Inject constructor(
    private val spellRepository: SpellsRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState

        @Immutable
        data class Loaded(
            val spell: Spell,
            val isFavorite: Boolean,
        ) : UiState

        @Immutable
        data class Error(val message: String) : UiState
    }

    private val spellId = MutableStateFlow<String?>(null)
    private val logger = Logger.createLogger<SpellDetailsViewModel>()

    val state: StateFlow<UiState> = combine(
        spellId,
        spellRepository.favoriteSpellIds,
    ) { id, favoriteIds ->
        SpellQuery(
            spellId = id,
            favoriteSpellIds = favoriteIds,
        )
    }
        .transformLatest { query ->
            val spellId = query.spellId
            if (spellId == null) {
                emit(UiState.Error("Spell not found."))
                return@transformLatest
            }

            emit(UiState.Loading)
            runCatching {
                val spell = spellRepository.getSpellById(spellId)
                if (spell == null) {
                    null
                } else {
                    UiState.Loaded(
                        spell = spell,
                        isFavorite = spellRepository.isFavorite(
                            spellId = spell.id,
                            favoriteSpellIds = query.favoriteSpellIds,
                        )
                    )
                }
            }.onSuccess { result ->
                emit(result ?: UiState.Error("Spell not found."))
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to load spell details." }
                emit(UiState.Error("Oops, something went wrong..."))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun loadSpell(spellId: String?) {
        this.spellId.value = spellId
    }

    fun toggleFavorite() {
        val currentSpellId = (state.value as? UiState.Loaded)?.spell?.id ?: return
        viewModelScope.launch {
            spellRepository.toggleFavorite(currentSpellId)
        }
    }

    private data class SpellQuery(
        val spellId: String?,
        val favoriteSpellIds: List<String>,
    )
}
