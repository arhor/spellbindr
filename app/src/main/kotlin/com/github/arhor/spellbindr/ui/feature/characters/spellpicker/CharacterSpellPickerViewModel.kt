package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    data class SpellsState(
        val query: String = "",
        val showFavoriteOnly: Boolean = false,
        val showFilterDialog: Boolean = false,
        val castingClasses: List<EntityRef> = emptyList(),
        val currentClasses: Set<EntityRef> = emptySet(),
        val uiState: SpellsUiState = SpellsUiState.Loading,
    )

    @Immutable
    sealed interface CharacterSpellPickerUiState {

        data object Loading : CharacterSpellPickerUiState

        data class Content(
            val sourceClass: String,
            val defaultSourceClass: String,
            val spellsState: SpellsState,
        ) : CharacterSpellPickerUiState

        data class Error(
            val message: String,
        ) : CharacterSpellPickerUiState
    }

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val isLoadingState = MutableStateFlow(characterId != null)
    private val errorMessageState = MutableStateFlow(if (characterId == null) "Missing character id" else null)
    private val sourceClassState = MutableStateFlow("")
    private val defaultSourceClassState = MutableStateFlow("")
    private val castingClassesState = MutableStateFlow<List<EntityRef>>(emptyList())
    private val spellFiltersState = MutableStateFlow(SpellFilters())
    private val _spellAssignments = MutableSharedFlow<CharacterSpellAssignment>()
    val spellAssignments = _spellAssignments.asSharedFlow()

    private val logger = Logger.createLogger<CharacterSpellPickerViewModel>()

    private val spellsUiState = combine(
        spellFiltersState,
        observeAllSpellsUseCase(),
        observeFavoriteSpellIdsUseCase(),
        ::SpellsQuery,
    )
        .debounce(350.milliseconds)
        .distinctUntilChanged()
        .transformLatest { data ->
            emit(SpellsUiState.Loading)
            runCatching {
                searchAndGroupSpellsUseCase(
                    query = data.query,
                    classes = data.currentClasses.toList(),
                    favoriteOnly = data.showFavorite,
                    allSpells = data.allSpells,
                    favoriteSpellIds = data.favoriteSpellIdsSet,
                )
            }.onSuccess { result ->
                emit(
                    SpellsUiState.Content(
                        query = data.query,
                        spells = result.spells,
                        showFavoriteOnly = false,
                        showFilterDialog = false,
                        castingClasses = emptyList(),
                        currentClasses = emptySet(),
                    )
                )
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to load spells." }
                emit(SpellsUiState.Failure("Oops, something went wrong..."))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    private val spellsState = combine(
        spellFiltersState,
        castingClassesState,
        spellsUiState,
    ) { filters, castingClasses, uiState ->
        SpellsState(
            query = filters.query,
            showFavoriteOnly = filters.showFavorite,
            showFilterDialog = filters.showFilterDialog,
            castingClasses = castingClasses,
            currentClasses = filters.currentClasses,
            uiState = uiState,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsState())

    val uiState: StateFlow<CharacterSpellPickerUiState> = combine(
        isLoadingState,
        errorMessageState,
        sourceClassState,
        defaultSourceClassState,
        spellsState,
    ) { isLoading, errorMessage, sourceClass, defaultSourceClass, spellsState ->
        when {
            isLoading -> CharacterSpellPickerUiState.Loading
            errorMessage != null -> CharacterSpellPickerUiState.Error(errorMessage)
            else -> CharacterSpellPickerUiState.Content(
                sourceClass = sourceClass,
                defaultSourceClass = defaultSourceClass,
                spellsState = spellsState,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CharacterSpellPickerUiState.Loading,
    )

    init {
        observeCharacter()
        observeCastingClasses()
    }

    fun onSourceClassChanged(value: String) {
        sourceClassState.value = value
    }

    fun onQueryChanged(query: String) {
        spellFiltersState.update { filters ->
            val trimmed = query.trim()
            if (trimmed.equals(filters.query, ignoreCase = true)) {
                filters
            } else {
                filters.copy(query = trimmed)
            }
        }
    }

    fun onFiltersClick() {
        spellFiltersState.update { filters -> filters.copy(showFilterDialog = true) }
    }

    fun onFavoritesClick() {
        spellFiltersState.update { filters -> filters.copy(showFavorite = !filters.showFavorite) }
    }

    fun onSubmitFilters(classes: Set<EntityRef>) {
        spellFiltersState.update { filters ->
            filters.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }

    fun onCancelFilters() {
        spellFiltersState.update { filters ->
            filters.copy(
                showFilterDialog = false,
                currentClasses = emptySet(),
            )
        }
    }

    fun onSpellSelected(spellId: String) {
        if (spellId.isBlank() || errorMessageState.value != null) return
        val resolvedSource = sourceClassState.value.ifBlank { defaultSourceClassState.value }
        viewModelScope.launch {
            _spellAssignments.emit(
                CharacterSpellAssignment(
                    spellId = spellId,
                    sourceClass = resolvedSource,
                )
            )
        }
    }

    private fun observeCharacter() {
        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    if (sheet != null) {
                        val className = sheet.className.trim()
                        defaultSourceClassState.value = className
                        if (sourceClassState.value.isBlank()) {
                            sourceClassState.value = className
                        }
                        isLoadingState.value = false
                        errorMessageState.value = null
                    } else {
                        errorMessageState.value = "Character not found"
                        isLoadingState.value = false
                    }
                }
                .catch { throwable ->
                    logger.error(throwable) { "Unable to load character." }
                    errorMessageState.value = throwable.message ?: "Unable to load character"
                    isLoadingState.value = false
                }
                .launchIn(viewModelScope)
        } ?: run {
            isLoadingState.value = false
        }
    }

    private fun observeCastingClasses() {
        viewModelScope.launch {
            castingClassesState.value = characterClassRepository.findSpellcastingClassesRefs()
        }
    }

    private data class SpellsQuery(
        val filters: SpellFilters,
        val allSpells: List<Spell>,
        val favoriteSpellIds: Set<String>,
    ) {
        val query: String = filters.query
        val currentClasses: Set<EntityRef> = filters.currentClasses
        val showFavorite: Boolean = filters.showFavorite
        val favoriteSpellIdsSet: Set<String> = favoriteSpellIds.toSet()
    }

    private data class SpellFilters(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val currentClasses: Set<EntityRef> = emptySet(),
    )
}
