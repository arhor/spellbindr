package com.github.arhor.spellbindr.ui.feature.character.spellpicker

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell

@Immutable
data class SpellcastingClassOption(
    val id: EntityRef,
    val name: String,
)

sealed interface CharacterSpellPickerUiState {

    @Immutable
    data object Loading : CharacterSpellPickerUiState

    @Immutable
    data class Content(
        val query: String,
        val showFavoriteOnly: Boolean,
        val spells: List<Spell>,
        val sourceClass: String,
        val defaultSourceClass: String,
        val spellcastingClassOptions: List<SpellcastingClassOption>,
        val selectedSpellcastingClass: SpellcastingClassOption?,
    ) : CharacterSpellPickerUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : CharacterSpellPickerUiState
}
