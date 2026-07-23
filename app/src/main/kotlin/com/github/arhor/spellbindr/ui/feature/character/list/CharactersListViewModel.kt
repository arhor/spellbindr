package com.github.arhor.spellbindr.ui.feature.character.list

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetsUseCase
import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class CharactersListViewModel @Inject constructor(
    private val observeCharacterSheets: ObserveCharacterSheetsUseCase,
) : ViewModel() {

    val uiState: StateFlow<CharactersListUiState> = observeCharacterSheets()
        .map {
            when (it) {
                is Loadable.Loading ->
                    CharactersListUiState.Loading

                is Loadable.Content ->
                    CharactersListUiState.Content(characters = it.data.map(::toListItem))

                is Loadable.Failure ->
                    CharactersListUiState.Failure(errorMessage = it.errorMessage ?: "Couldn't load characters")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CharactersListUiState.Loading)

    private val _effects = MutableSharedFlow<CharactersListEffect>()
    val effects: SharedFlow<CharactersListEffect> = _effects.asSharedFlow()

    fun dispatch(intent: CharactersListIntent) {
        when (intent) {
            is CharactersListIntent.SelectCharacterClicked -> {
                viewModelScope.launch {
                    _effects.emit(CharactersListEffect.CharacterSelected(intent.character))
                }
            }

            is CharactersListIntent.CreateCharacterClicked -> {
                viewModelScope.launch {
                    _effects.emit(CharactersListEffect.CreateCharacterSelected(intent.mode))
                }
            }
        }
    }

    private fun toListItem(sheet: CharacterSheet): CharacterListItem = CharacterListItem(
        id = sheet.id,
        name = sheet.name,
        level = sheet.level,
        className = sheet.className,
        race = sheet.race,
        background = sheet.background,
    )
}
