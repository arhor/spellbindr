package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FakeCharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
import com.github.arhor.spellbindr.domain.repository.FakeFavoritesRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerViewModel.CharacterSpellPickerUiState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterSpellPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onFavoritesClick should toggle favorites filter`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onFavoritesClick()
        val contentState = viewModel.awaitContentState { it.spellsState.showFavorite }

        // Then
        assertThat(contentState.spellsState.showFavorite).isTrue()
    }

    @Test
    fun `onSubmitFilters should close dialog and update selected classes`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()
        val wizard = EntityRef("wizard")

        // When
        viewModel.onFiltersClick()
        viewModel.onSubmitFilters(setOf(wizard))
        val contentState = viewModel.awaitContentState {
            !it.spellsState.showFilterDialog && wizard in it.spellsState.currentClasses
        }

        // Then
        assertThat(contentState.spellsState.showFilterDialog).isFalse()
        assertThat(contentState.spellsState.currentClasses).containsExactly(wizard)
    }

    @Test
    fun `onSpellGroupToggled should update expansion state`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceTimeBy(400)
        advanceUntilIdle()

        // When
        viewModel.onSpellGroupToggled(level = 1)
        val contentState = viewModel.awaitContentState { it.spellsState.expandedSpellLevels[1] == false }

        // Then
        assertThat(contentState.spellsState.expandedSpellLevels[1]).isFalse()
    }

    @Test
    fun `onSpellSelected should emit assignment with resolved source class`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceTimeBy(400)
        advanceUntilIdle()
        val spellsState = viewModel.awaitContentState { it.spellsState.uiState is SpellsUiState.Content }
        val spellId = (spellsState.spellsState.uiState as SpellsUiState.Content).spells.first().id

        // When
        val assignmentDeferred = async { viewModel.spellAssignments.first() }
        viewModel.onSpellSelected(spellId)
        val assignment = assignmentDeferred.await()

        // Then
        assertThat(assignment).isEqualTo(
            CharacterSpellAssignment(
                spellId = spellId,
                sourceClass = "Wizard",
            )
        )
    }
}

private suspend fun CharacterSpellPickerViewModel.awaitContentState(
    predicate: (CharacterSpellPickerUiState.Content) -> Boolean = { true },
): CharacterSpellPickerUiState.Content = uiState.first { state ->
    state is CharacterSpellPickerUiState.Content && predicate(state)
} as CharacterSpellPickerUiState.Content

private fun TestScope.createViewModel(): CharacterSpellPickerViewModel {
    val spells = listOf(
        Spell(
            id = "magic-missile",
            name = "Magic Missile",
            desc = listOf("Force darts"),
            level = 1,
            range = "120 feet",
            ritual = false,
            school = EntityRef("evocation"),
            duration = "Instantaneous",
            castingTime = "1 action",
            classes = listOf(EntityRef("wizard")),
            components = listOf("V", "S"),
            concentration = false,
            source = "srd",
        ),
    )
    val characterRepository = FakeCharacterRepository(
        initialSheets = listOf(
            CharacterSheet(
                id = CHARACTER_ID,
                className = "Wizard",
            )
        )
    )
    val spellsRepository = FakeSpellsRepository(initialSpells = spells)
    val favoritesRepository = FakeFavoritesRepository()
    val classRepository = FakeCharacterClassRepository(
        spellcastingClassesRefs = listOf(EntityRef("wizard"), EntityRef("cleric")),
    )

    return CharacterSpellPickerViewModel(
        characterRepository = characterRepository,
        characterClassRepository = classRepository,
        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
        observeFavoriteSpellIdsUseCase = ObserveFavoriteSpellIdsUseCase(favoritesRepository),
        searchAndGroupSpellsUseCase = SearchAndGroupSpellsUseCase(spellsRepository, favoritesRepository),
        savedStateHandle = SavedStateHandle(mapOf("characterId" to CHARACTER_ID)),
    )
}

private const val CHARACTER_ID = "character-1"
