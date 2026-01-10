package com.github.arhor.spellbindr.ui.feature.characters.list

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.ui.feature.characters.list.model.CharacterListItem

sealed interface CharactersListUiState {

    @Immutable
    data object Loading : CharactersListUiState

    @Immutable
    data class Content(
        val characters: List<CharacterListItem>,
    ) : CharactersListUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : CharactersListUiState
}
