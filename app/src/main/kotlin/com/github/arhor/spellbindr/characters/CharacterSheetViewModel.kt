package com.github.arhor.spellbindr.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    repository: CharacterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val characterId: String? = savedStateHandle.get<String>("characterId")

    val uiState: StateFlow<CharacterSheetUiState> =
        characterId?.let { id ->
            repository.observeCharacterSheet(id)
                .map { sheet ->
                    if (sheet != null) {
                        CharacterSheetUiState(
                            isLoading = false,
                            characterId = id,
                            character = sheet,
                            errorMessage = null,
                        )
                    } else {
                        CharacterSheetUiState(
                            isLoading = false,
                            characterId = id,
                            character = null,
                            errorMessage = "Character not found",
                        )
                    }
                }
                .catch { throwable ->
                    emit(
                        CharacterSheetUiState(
                            isLoading = false,
                            characterId = id,
                            errorMessage = throwable.message ?: "Unable to load character",
                        )
                    )
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CharacterSheetUiState(isLoading = true, characterId = id),
                )
        } ?: MutableStateFlow(
            CharacterSheetUiState(
                isLoading = false,
                errorMessage = "Missing character id",
            )
        )
}

@Immutable
data class CharacterSheetUiState(
    val isLoading: Boolean = false,
    val characterId: String? = null,
    val character: CharacterSheet? = null,
    val errorMessage: String? = null,
)
