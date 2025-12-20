package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.domain.repository.CharactersRepository
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.SpellListState
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val characterRepository: CharactersRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val spellRepository: SpellsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    data class SpellsState(
        override val query: String = "",
        override val showFavorite: Boolean = false,
        override val showFilterDialog: Boolean = false,
        override val castingClasses: List<EntityRef> = emptyList(),
        override val currentClasses: Set<EntityRef> = emptySet(),
        override val uiState: CompendiumViewModel.SpellsUiState = CompendiumViewModel.SpellsUiState.Loading,
        override val spellsByLevel: Map<Int, List<Spell>> = emptyMap(),
        override val expandedSpellLevels: Map<Int, Boolean> = emptyMap(),
        override val expandedAll: Boolean = true,
    ) : SpellListState

    @Immutable
    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sourceClass: String = "",
        val defaultSourceClass: String = "",
        val spellsState: SpellsState = SpellsState(),
    )

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _state = MutableStateFlow(
        State(
            isLoading = characterId != null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        )
    )
    val state: StateFlow<State> = _state
    private val logger = Logger.createLogger<CharacterSpellPickerViewModel>()

    init {
        observeStateChanges()
        viewModelScope.launch {
            characterClassRepository
                .findSpellcastingClassesRefs()
                .let { refs ->
                    _state.update {
                        it.copy(
                            spellsState = it.spellsState.copy(
                                castingClasses = refs,
                            )
                        )
                    }
                }
        }
        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    if (sheet != null) {
                        _state.update { state ->
                            val fallback = sheet.className.trim()
                            state.copy(
                                isLoading = false,
                                defaultSourceClass = fallback,
                                sourceClass = if (state.sourceClass.isBlank()) fallback else state.sourceClass,
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Character not found",
                            )
                        }
                    }
                }
                .catch { throwable ->
                    _state.update {
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
        _state.update { it.copy(sourceClass = value) }
    }

    fun buildAssignment(spellId: String): CharacterSpellAssignment? {
        val state = _state.value
        if (spellId.isBlank() || state.errorMessage != null) return null
        val resolvedSource = state.sourceClass.takeIf { it.isNotBlank() } ?: state.defaultSourceClass
        return CharacterSpellAssignment(
            spellId = spellId,
            sourceClass = resolvedSource,
        )
    }

    fun onFavoritesClicked() {
        _state.update {
            it.copy(
                spellsState = it.spellsState.copy(
                    showFavorite = !it.spellsState.showFavorite
                )
            )
        }
    }

    fun onSpellGroupToggled(level: Int) {
        _state.update { state ->
            val currentExpanded = state.spellsState.expandedSpellLevels[level] ?: state.spellsState.expandedAll
            state.copy(
                spellsState = state.spellsState.copy(
                    expandedSpellLevels = state.spellsState.expandedSpellLevels + (level to !currentExpanded),
                )
            )
        }
    }

    fun onToggleAllSpellGroups() {
        _state.update { state ->
            val nextExpandedAll = !state.spellsState.expandedAll
            val levels = state.spellsState.spellsByLevel.keys
            state.copy(
                spellsState = state.spellsState.copy(
                    expandedAll = nextExpandedAll,
                    expandedSpellLevels = levels.associateWith { nextExpandedAll },
                )
            )
        }
    }

    fun onFilterClicked() {
        _state.update {
            it.copy(
                spellsState = it.spellsState.copy(
                    showFilterDialog = true
                )
            )
        }
    }

    fun onQueryChanged(query: String) {
        val currQuery = _state.value.spellsState.query
        val nextQuery = query.trim()

        if (!nextQuery.equals(currQuery, ignoreCase = true)) {
            _state.update {
                it.copy(
                    spellsState = it.spellsState.copy(
                        query = nextQuery
                    )
                )
            }
        }
    }

    fun onFilterChanged(classes: Set<EntityRef>) {
        _state.update {
            if (classes != _state.value.spellsState.currentClasses) {
                it.copy(
                    spellsState = it.spellsState.copy(
                        showFilterDialog = false,
                        currentClasses = classes,
                    )
                )
            } else {
                it.copy(
                    spellsState = it.spellsState.copy(
                        showFilterDialog = false,
                    )
                )
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        viewModelScope.launch {
            combine(_state, spellRepository.allSpells, spellRepository.favoriteSpellIds, ::toObservableData)
                .debounce(350.milliseconds)
                .distinctUntilChanged()
                .collect { data ->
                    try {
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    uiState = CompendiumViewModel.SpellsUiState.Loading,
                                )
                            )
                        }
                        val spells = spellRepository.findSpells(
                            query = data.query,
                            classes = data.currentClasses,
                            favoriteOnly = data.showFavorite,
                        )
                        val spellsByLevel = spells.groupBy(Spell::level).toSortedMap()
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    uiState = CompendiumViewModel.SpellsUiState.Loaded(
                                        spells = spells,
                                        spellsByLevel = spellsByLevel,
                                    ),
                                    spellsByLevel = spellsByLevel,
                                    expandedSpellLevels = ensureExpandedLevels(
                                        currentExpanded = it.spellsState.expandedSpellLevels,
                                        levels = spellsByLevel.keys,
                                        expandedAll = it.spellsState.expandedAll,
                                    ),
                                )
                            )
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to load spells." }
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    uiState = CompendiumViewModel.SpellsUiState.Error(
                                        "Oops, something went wrong..."
                                    ),
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun toObservableData(
        state: State, allSpells: List<Spell>, favSpells: List<String>
    ): ObservableData = ObservableData(
        state.spellsState.query,
        state.spellsState.castingClasses,
        state.spellsState.currentClasses,
        state.spellsState.showFavorite,
        allSpells,
        favSpells,
    )

    private data class ObservableData(
        val query: String,
        val castingClasses: List<EntityRef>,
        val currentClasses: Set<EntityRef>,
        val showFavorite: Boolean,
        val allSpells: List<Spell>,
        val favSpells: List<String>,
    )

    private fun ensureExpandedLevels(
        currentExpanded: Map<Int, Boolean>,
        levels: Set<Int>,
        expandedAll: Boolean,
    ): Map<Int, Boolean> = levels.associateWith { level ->
        currentExpanded[level] ?: expandedAll
    }

}
