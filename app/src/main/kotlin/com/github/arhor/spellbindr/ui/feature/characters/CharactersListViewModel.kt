package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Stable
@HiltViewModel
class CharactersListViewModel @Inject constructor(
    observeCharacterSheetsUseCase: ObserveCharacterSheetsUseCase,
) : ViewModel() {

    val state: StateFlow<CharactersListUiState> = observeCharacterSheetsUseCase()
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
    val level: Int,
    val className: String,
    val race: String,
    val background: String,
)

private fun CharacterSheet.toListItem(): CharacterListItem = CharacterListItem(
    id = id,
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
)
