package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val observeSpells: ObserveSpellsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    private data class State(
        val sourceClass: String = "",
        val defaultSourceClass: String = "",
        val query: String = "",
        val showFavoriteOnly: Boolean = false,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    @Immutable
    private data class Filters(
        val query: String,
        val classes: Set<EntityRef>,
        val favoritesOnly: Boolean,
    )

    @Immutable
    sealed interface CharacterSpellPickerUiState {

        data object Loading : CharacterSpellPickerUiState

        data class Content(
            val sourceClass: String,
            val defaultSourceClass: String,
            val spellsUiState: SpellsUiState,
        ) : CharacterSpellPickerUiState

        data class Error(
            val message: String,
        ) : CharacterSpellPickerUiState
    }

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _state = MutableStateFlow(
        State(
            isLoading = characterId != null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        ),
    )
    private val _spellAssignments = MutableSharedFlow<CharacterSpellAssignment>()
    val spellAssignments = _spellAssignments.asSharedFlow()

    private val logger = Logger.createLogger<CharacterSpellPickerViewModel>()

    private val spellsUiState = combine(
        _state,
        observeSpellsUsingFilters(),
    ) { state, spells ->
        when (spells) {
            is Loadable.Ready -> SpellsUiState.Content(
                query = state.query,
                spells = spells.data,
                showFavoriteOnly = state.showFavoriteOnly,
                showFilterDialog = false,
                castingClasses = emptyList(),
                currentClasses = emptySet(),
            )

            is Loadable.Error -> SpellsUiState.Failure("Failed to load spells.")

            is Loadable.Loading -> SpellsUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    val uiState: StateFlow<CharacterSpellPickerUiState> = combine(
        _state,
        spellsUiState,
    ) { state, spellsState ->
        when {
            state.isLoading -> CharacterSpellPickerUiState.Loading
            state.errorMessage != null -> CharacterSpellPickerUiState.Error(state.errorMessage)
            else -> CharacterSpellPickerUiState.Content(
                sourceClass = state.sourceClass,
                defaultSourceClass = state.defaultSourceClass,
                spellsUiState = spellsState,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CharacterSpellPickerUiState.Loading,
    )

    init {
        characterId?.let(::observeCharacter)
    }

    fun onSourceClassChanged(value: String) {
        _state.update { it.copy(sourceClass = value) }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onFavoritesToggled() {
        _state.update { it.copy(showFavoriteOnly = !it.showFavoriteOnly) }
    }

    fun onSpellSelected(spellId: String) {
        if (spellId.isBlank() || _state.value.errorMessage != null) return
        val resolvedSource = resolveSourceClass(
            sourceClass = _state.value.sourceClass,
            defaultSourceClass = _state.value.defaultSourceClass,
        )
        viewModelScope.launch {
            _spellAssignments.emit(
                CharacterSpellAssignment(
                    spellId = spellId,
                    sourceClass = resolvedSource,
                )
            )
        }
    }

    private fun observeCharacter(id: String) {
        characterRepository.observeCharacterSheet(id)
            .onEach { sheet ->
                _state.update { state ->
                    if (sheet != null) {
                        val className = sheet.className.trim()
                        state.copy(
                            sourceClass = if (state.sourceClass.isBlank()) className else state.sourceClass,
                            defaultSourceClass = className,
                            isLoading = false,
                            errorMessage = null,
                        )
                    } else {
                        state.copy(
                            isLoading = false,
                            errorMessage = "Character not found",
                        )
                    }
                }
            }
            .catch { throwable ->
                logger.error(throwable) { "Unable to load character." }
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load character",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSpellsUsingFilters(): Flow<Loadable<List<Spell>>> {
        return combine(
            _state.map { it.query.trim() }.distinctUntilChanged().debounce { if (it.isBlank()) 0L else 350L },
            _state.map { resolveClassFilter(it.sourceClass, it.defaultSourceClass) }.distinctUntilChanged(),
            _state.map { it.showFavoriteOnly }.distinctUntilChanged(),
        ) { query, classes, favoriteOnly -> Filters(query, classes, favoriteOnly) }
            .distinctUntilChanged()
            .flatMapLatest { observeSpells(it.query, it.classes, it.favoritesOnly) }
    }

    private fun resolveClassFilter(sourceClass: String, defaultSourceClass: String): Set<EntityRef> {
        val resolved = resolveSourceClass(sourceClass, defaultSourceClass).lowercase()
        return if (resolved.isBlank()) emptySet() else setOf(EntityRef(resolved))
    }

    private fun resolveSourceClass(sourceClass: String, defaultSourceClass: String): String =
        sourceClass.ifBlank { defaultSourceClass }.trim()
}
