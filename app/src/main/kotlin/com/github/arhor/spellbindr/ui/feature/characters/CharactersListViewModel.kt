package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.repository.CharactersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Stable
@HiltViewModel
class CharactersListViewModel @Inject constructor(
    repository: CharactersRepository,
) : ViewModel() {

    val state: StateFlow<CharactersListUiState> = repository.observeCharacterSheets()
        .map { sheets ->
            CharactersListUiState(
                characters = sheets.map { it.toListItem() },
                isEmpty = sheets.isEmpty(),
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharactersListUiState(isLoading = true),
        )
}

@Immutable
data class CharactersListUiState(
    val characters: List<CharacterListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
)

@Immutable
data class CharacterListItem(
    val id: String,
    val name: String,
    val headline: String,
    val detail: String,
)

private fun CharacterSheet.toListItem(): CharacterListItem = CharacterListItem(
    id = id,
    name = name.ifBlank { "Unnamed hero" },
    headline = buildString {
        append("Level ${level.coerceAtLeast(1)}")
        if (className.isNotBlank()) {
            append(' ')
            append(className)
        }
    },
    detail = listOfNotNull(
        race.takeIf { it.isNotBlank() },
        background.takeIf { it.isNotBlank() },
    ).joinToString(separator = " • "),
)
