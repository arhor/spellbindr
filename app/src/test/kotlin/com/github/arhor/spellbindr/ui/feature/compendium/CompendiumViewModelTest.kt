package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FakeAlignmentRepository
import com.github.arhor.spellbindr.domain.repository.FakeCharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.FakeFavoritesRepository
import com.github.arhor.spellbindr.domain.repository.FakeRacesRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.repository.FakeTraitsRepository
import com.github.arhor.spellbindr.domain.usecase.GetSpellcastingClassRefsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAlignmentsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompendiumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onAction should update selected section when section selection occurs`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onAction(CompendiumViewModel.CompendiumAction.SectionSelected(CompendiumSection.Races))
        val state = viewModel.awaitContentState { it.selectedSection == CompendiumSection.Races }

        // Then
        assertThat(state.selectedSection).isEqualTo(CompendiumSection.Races)
    }

    @Test
    fun `onAction should trim and update spell query when query changes`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onAction(CompendiumViewModel.CompendiumAction.SpellQueryChanged("  Fire "))
        val state = viewModel.awaitContentState { it.spellsState.query == "Fire" }

        // Then
        assertThat(state.spellsState.query).isEqualTo("Fire")
    }

    @Test
    fun `onAction should update favorites filter when toggle is triggered`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onAction(CompendiumViewModel.CompendiumAction.SpellFavoritesToggled)
        val state = viewModel.awaitContentState { it.spellsState.showFavorite }

        // Then
        assertThat(state.spellsState.showFavorite).isTrue()
    }

    @Test
    fun `onAction should flip spell group expansion state when level is toggled`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceTimeBy(400)
        advanceUntilIdle()

        // When
        viewModel.onAction(CompendiumViewModel.CompendiumAction.SpellGroupToggled(level = 1))
        val state = viewModel.awaitContentState { it.spellsState.expandedSpellLevels[1] == false }

        // Then
        assertThat(state.spellsState.expandedSpellLevels[1]).isFalse()
    }
}

private suspend fun CompendiumViewModel.awaitContentState(
    predicate: (CompendiumViewModel.CompendiumUiState.Content) -> Boolean = { true },
): CompendiumViewModel.CompendiumUiState.Content =
    uiState.first { state ->
        state is CompendiumViewModel.CompendiumUiState.Content && predicate(state)
    } as CompendiumViewModel.CompendiumUiState.Content

private fun TestScope.createViewModel(): CompendiumViewModel {
    val spells = listOf(
        Spell(
            id = "fire-bolt",
            name = "Fire Bolt",
            desc = listOf("Deals fire damage"),
            level = 0,
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
    val spellsRepository = FakeSpellsRepository(initialSpells = spells)
    val favoritesRepository = FakeFavoritesRepository()
    val alignmentsRepository = FakeAlignmentRepository()
    val racesRepository = FakeRacesRepository()
    val traitsRepository = FakeTraitsRepository()
    val classRepository = FakeCharacterClassRepository()

    return CompendiumViewModel(
        savedStateHandle = SavedStateHandle(),
        getSpellcastingClassRefsUseCase = GetSpellcastingClassRefsUseCase(classRepository),
        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
        observeAlignmentsUseCase = ObserveAlignmentsUseCase(alignmentsRepository),
        observeFavoriteSpellIdsUseCase = ObserveFavoriteSpellIdsUseCase(favoritesRepository),
        observeRacesUseCase = ObserveRacesUseCase(racesRepository),
        observeTraitsUseCase = ObserveTraitsUseCase(traitsRepository),
        searchAndGroupSpellsUseCase = SearchAndGroupSpellsUseCase(spellsRepository, favoritesRepository),
    )
}
