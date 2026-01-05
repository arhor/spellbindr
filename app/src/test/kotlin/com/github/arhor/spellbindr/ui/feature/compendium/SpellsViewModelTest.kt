package com.github.arhor.spellbindr.ui.feature.compendium

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FakeCharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.FakeFavoritesRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.usecase.GetSpellcastingClassRefsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsStateUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SpellsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onQueryChanged should trim and update spell query`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onQueryChanged("  Fire ")
        val state = viewModel.awaitUiState { it.query == "Fire" }

        // Then
        assertThat(state.query).isEqualTo("Fire")
    }

    @Test
    fun `onFavoritesToggled should update favorites filter`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onFavoritesToggled()
        val state = viewModel.awaitUiState { it.showFavorite }

        // Then
        assertThat(state.showFavorite).isTrue()
    }

    @Test
    fun `onGroupToggled should flip spell group expansion state`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceTimeBy(400)
        advanceUntilIdle()

        // When
        viewModel.onGroupToggled(level = 1)
        val state = viewModel.awaitUiState { it.expandedSpellLevels[1] == false }

        // Then
        assertThat(state.expandedSpellLevels[1]).isFalse()
    }
}

private suspend fun SpellsViewModel.awaitUiState(
    predicate: (SpellsViewModel.State) -> Boolean = { true },
): SpellsViewModel.State = uiState.first(predicate)

private fun TestScope.createViewModel(): SpellsViewModel {
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
    val classRepository = FakeCharacterClassRepository()

    return SpellsViewModel(
        getSpellcastingClassRefsUseCase = GetSpellcastingClassRefsUseCase(classRepository),
        observeAllSpellsStateUseCase = ObserveAllSpellsStateUseCase(spellsRepository),
        observeFavoriteSpellIdsUseCase = ObserveFavoriteSpellIdsUseCase(favoritesRepository),
        searchAndGroupSpellsUseCase = SearchAndGroupSpellsUseCase(spellsRepository, favoritesRepository),
    )
}
