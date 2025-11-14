package com.github.arhor.spellbindr.ui.feature.characters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val characterId: String? = savedStateHandle.get<String>("characterId")

    private val _uiState = MutableStateFlow(
        CharacterSpellPickerUiState(
            isLoading = characterId != null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        )
    )
    val uiState: StateFlow<CharacterSpellPickerUiState> = _uiState

    init {
        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    if (sheet != null) {
                        _uiState.update { state ->
                            val fallback = sheet.className.trim()
                            state.copy(
                                isLoading = false,
                                defaultSourceClass = fallback,
                                sourceClass = if (state.sourceClass.isBlank()) fallback else state.sourceClass,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Character not found",
                            )
                        }
                    }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load character",
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onSourceClassChanged(value: String) {
        _uiState.update { it.copy(sourceClass = value) }
    }

    fun buildAssignment(spellId: String): CharacterSpellAssignment? {
        val state = _uiState.value
        if (spellId.isBlank() || state.errorMessage != null) return null
        val resolvedSource = state.sourceClass.takeIf { it.isNotBlank() } ?: state.defaultSourceClass
        return CharacterSpellAssignment(
            spellId = spellId,
            sourceClass = resolvedSource,
        )
    }
}

data class CharacterSpellPickerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sourceClass: String = "",
    val defaultSourceClass: String = "",
)
