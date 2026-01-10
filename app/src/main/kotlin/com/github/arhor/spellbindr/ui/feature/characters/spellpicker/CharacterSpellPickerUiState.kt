package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell

sealed interface CharacterSpellPickerUiState {

    @Immutable
    data object Loading : CharacterSpellPickerUiState

    @Immutable
    data class Content(
        val query: String,
        val showFavoriteOnly: Boolean,
        val sourceClass: String,
        val defaultSourceClass: String,
        val spells: List<Spell>,
        val castingClasses: List<EntityRef>,
        val currentClasses: Set<EntityRef>,
    ) : CharacterSpellPickerUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : CharacterSpellPickerUiState
}
