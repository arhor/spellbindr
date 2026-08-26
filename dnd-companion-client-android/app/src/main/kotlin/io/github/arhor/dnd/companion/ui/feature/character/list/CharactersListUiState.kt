package io.github.arhor.dnd.companion.ui.feature.character.list

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.ui.feature.character.list.model.CharacterListItem

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
