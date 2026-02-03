package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val observeCharacterSheet: ObserveCharacterSheetUseCase,
    private val observeSpells: ObserveSpellsUseCase,
    private val observeSpellcastingClasses: ObserveSpellcastingClassesUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    private data class State(
        val query: String = "",
        val sourceClass: String = "",
        val selectedSpellcastingClass: EntityRef? = null,
        val showFavoriteOnly: Boolean = false,
    )

    @Immutable
    private data class SourceContext(
        val defaultSourceClass: String,
        val spellcastingClassOptions: List<SpellcastingClassOption>,
    )

    @Immutable
    private data class Filters(
        val query: String,
        val classes: Set<EntityRef>,
        val favoritesOnly: Boolean,
    )

    private val _state = MutableStateFlow(State())
    private val characterId: String = savedStateHandle.toRoute<AppDestination.CharacterSpellPicker>().characterId

    private val characterSheetState = observeCharacterSheet(characterId)
    private val sourceContext = combine(
        characterSheetState,
        observeSpellcastingClasses(),
    ) { sheetState, spellcastingClassesState ->
        val sheet = (sheetState as? Loadable.Content)?.data
        val rawClassName = sheet?.className.orEmpty().trim()
        val defaultSourceClass = rawClassName.extractPrimaryClassName()
        val spellcastingOptions = when {
            sheet == null -> emptyList()
            spellcastingClassesState is Loadable.Content ->
                resolveSpellcastingClassOptions(
                    className = rawClassName,
                    spellcastingClasses = spellcastingClassesState.data,
                )

            else -> emptyList()
        }
        SourceContext(
            defaultSourceClass = defaultSourceClass,
            spellcastingClassOptions = spellcastingOptions,
        )
    }.distinctUntilChanged()

    val uiState: StateFlow<CharacterSpellPickerUiState> = combine(
        _state,
        observeSpellsUsingFilters(),
        characterSheetState,
        sourceContext,
    ) { state, spellsState, sheetState, sourceContext ->
        when {
            spellsState is Loadable.Content && sheetState is Loadable.Content && sheetState.data != null -> {
                val selected = state.selectedSpellcastingClass
                    ?.let { selection -> sourceContext.spellcastingClassOptions.firstOrNull { it.id == selection } }
                    ?: sourceContext.spellcastingClassOptions.firstOrNull()

                CharacterSpellPickerUiState.Content(
                    query = state.query,
                    spells = spellsState.data,
                    showFavoriteOnly = state.showFavoriteOnly,
                    sourceClass = state.sourceClass,
                    defaultSourceClass = sourceContext.defaultSourceClass,
                    spellcastingClassOptions = sourceContext.spellcastingClassOptions,
                    selectedSpellcastingClass = selected,
                )
            }

            spellsState is Loadable.Failure -> CharacterSpellPickerUiState.Failure("Failed to load spells.")
            sheetState is Loadable.Failure -> CharacterSpellPickerUiState.Failure("Failed to load character.")
            sheetState is Loadable.Content && sheetState.data == null -> CharacterSpellPickerUiState.Failure("Character not found.")

            else -> CharacterSpellPickerUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CharacterSpellPickerUiState.Loading)

    fun onSourceClassChanged(value: String) {
        _state.update { it.copy(sourceClass = value) }
    }

    fun onSpellcastingClassSelected(value: EntityRef) {
        _state.update { it.copy(selectedSpellcastingClass = value) }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onFavoritesToggled() {
        _state.update { it.copy(showFavoriteOnly = !it.showFavoriteOnly) }
    }

    private fun observeSpellsUsingFilters(): Flow<Loadable<List<Spell>>> {
        return combine(
            _state.map { it.query.trim() }.distinctUntilChanged().debounce { if (it.isBlank()) 0L else 350L },
            _state.map { it.showFavoriteOnly }.distinctUntilChanged(),
            _state.map { it.sourceClass }.distinctUntilChanged(),
            _state.map { it.selectedSpellcastingClass }.distinctUntilChanged(),
            sourceContext,
        ) { query, favoriteOnly, sourceClass, selectedSpellcastingClass, sourceContext ->
            val classes = resolveClassFilter(
                sourceClass = sourceClass,
                defaultSourceClass = sourceContext.defaultSourceClass,
                selectedSpellcastingClass = selectedSpellcastingClass,
                spellcastingClassOptions = sourceContext.spellcastingClassOptions,
            )
            Filters(query, classes, favoriteOnly)
        }
            .distinctUntilChanged()
            .flatMapLatest { observeSpells(it.query, it.classes, it.favoritesOnly) }
    }

    private fun resolveClassFilter(
        sourceClass: String,
        defaultSourceClass: String,
        selectedSpellcastingClass: EntityRef?,
        spellcastingClassOptions: List<SpellcastingClassOption>,
    ): Set<EntityRef> {
        if (spellcastingClassOptions.isNotEmpty()) {
            val selected = spellcastingClassOptions.firstOrNull { it.id == selectedSpellcastingClass }
                ?: spellcastingClassOptions.first()
            return setOf(selected.id)
        }

        val normalized = sourceClass.ifBlank { defaultSourceClass }.toEntityRefId()
        return normalized?.let { setOf(EntityRef(it)) } ?: emptySet()
    }

    private fun resolveSpellcastingClassOptions(
        className: String,
        spellcastingClasses: List<CharacterClass>,
    ): List<SpellcastingClassOption> {
        val normalizedClassName = className.trim()
        if (normalizedClassName.isBlank()) return emptyList()

        val classNameLowercase = normalizedClassName.lowercase()
        return spellcastingClasses
            .mapNotNull { clazz ->
                val nameIndex = classNameLowercase.indexOf(clazz.name.lowercase())
                val idIndex = classNameLowercase.indexOf(clazz.id.lowercase())
                val index = listOf(nameIndex, idIndex).filter { it >= 0 }.minOrNull() ?: return@mapNotNull null
                clazz to index
            }
            .sortedWith(
                compareBy<Pair<CharacterClass, Int>> { it.second }
                    .thenBy { it.first.name.lowercase() },
            )
            .map { (clazz, _) ->
                SpellcastingClassOption(
                    id = EntityRef(clazz.id),
                    name = clazz.name,
                )
            }
    }
}

private fun String.toEntityRefId(): String? {
    val normalized = trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
    return normalized.takeIf { it.isNotBlank() }
}

private fun String.extractPrimaryClassName(): String {
    val raw = trim()
    if (raw.isBlank()) return ""

    val firstSegment = raw.split(Regex("[/,&;|]+")).firstOrNull().orEmpty().trim()
    if (firstSegment.isBlank()) return raw

    return firstSegment
        .replace(Regex("\\s*\\d+\\s*$"), "")
        .trim()
        .ifBlank { firstSegment }
}
