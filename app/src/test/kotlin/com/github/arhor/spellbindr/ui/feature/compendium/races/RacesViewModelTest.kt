package com.github.arhor.spellbindr.ui.feature.compendium.races

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RacesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeRaces = mockk<ObserveAllRacesUseCase>()
    private val observeTraits = mockk<ObserveAllTraitsUseCase>()

    @Test
    fun `uiState should emit loading then content when races and traits are available`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val trait = Trait(
                id = "darkvision",
                name = "Darkvision",
                desc = listOf("Can see in dim light."),
            )
            val race = Race(
                id = "elf",
                name = "Elf",
                traits = listOf(EntityRef(trait.id)),
                subraces = emptyList(),
            )
            val racesFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Loading)
            val traitsFlow = MutableStateFlow<Loadable<List<Trait>>>(Loadable.Loading)
            every { observeRaces() } returns racesFlow
            every { observeTraits() } returns traitsFlow
            val viewModel = RacesViewModel(observeRaces, observeTraits)
            val states = mutableListOf<RacesUiState>()

            // When
            val job = launch {
                viewModel.uiState.take(2).toList(states)
            }
            racesFlow.value = Loadable.Content(listOf(race))
            traitsFlow.value = Loadable.Content(listOf(trait))
            advanceUntilIdle()
            job.join()

            // Then
            assertThat(states).containsExactly(
                RacesUiState.Loading,
                RacesUiState.Content(
                    races = listOf(race),
                    traits = mapOf(trait.id to trait),
                    selectedItemId = null,
                ),
            ).inOrder()
            verify(exactly = 1) { observeRaces() }
            verify(exactly = 1) { observeTraits() }
        }

    @Test
    fun `uiState should emit error message when races use case fails without message`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val racesFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Loading)
            val traitsFlow = MutableStateFlow<Loadable<List<Trait>>>(Loadable.Content(emptyList()))
            every { observeRaces() } returns racesFlow
            every { observeTraits() } returns traitsFlow
            val viewModel = RacesViewModel(observeRaces, observeTraits)

            // When
            val errorState = async { viewModel.uiState.first { it is RacesUiState.Error } }
            racesFlow.value = Loadable.Failure()
            advanceUntilIdle()

            // Then
            assertThat(errorState.await()).isEqualTo(RacesUiState.Error("Failed to load races"))
        }

    @Test
    fun `uiState should emit error message when traits use case fails with message`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val racesFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Content(emptyList()))
            val traitsFlow = MutableStateFlow<Loadable<List<Trait>>>(Loadable.Loading)
            every { observeRaces() } returns racesFlow
            every { observeTraits() } returns traitsFlow
            val viewModel = RacesViewModel(observeRaces, observeTraits)

            // When
            val errorState = async { viewModel.uiState.first { it is RacesUiState.Error } }
            traitsFlow.value = Loadable.Failure(errorMessage = "Traits failed")
            advanceUntilIdle()

            // Then
            assertThat(errorState.await()).isEqualTo(RacesUiState.Error("Traits failed"))
        }

    @Test
    fun `onRaceClick should select race when new race tapped`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val trait = Trait(
            id = "keen_senses",
            name = "Keen Senses",
            desc = listOf("Proficiency in Perception."),
        )
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef(trait.id)),
            subraces = emptyList(),
        )
        val racesFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Content(listOf(race)))
        val traitsFlow = MutableStateFlow<Loadable<List<Trait>>>(Loadable.Content(listOf(trait)))
        every { observeRaces() } returns racesFlow
        every { observeTraits() } returns traitsFlow
        val viewModel = RacesViewModel(observeRaces, observeTraits)
        advanceUntilIdle()

        // When
        viewModel.onRaceClick(race)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first {
            it is RacesUiState.Content && it.selectedItemId == race.id
        } as RacesUiState.Content
        assertThat(state.selectedItemId).isEqualTo(race.id)
    }

    @Test
    fun `onRaceClick should clear selection when same race tapped again`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val trait = Trait(
            id = "darkvision",
            name = "Darkvision",
            desc = listOf("Can see in dim light."),
        )
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef(trait.id)),
            subraces = emptyList(),
        )
        val racesFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Content(listOf(race)))
        val traitsFlow = MutableStateFlow<Loadable<List<Trait>>>(Loadable.Content(listOf(trait)))
        every { observeRaces() } returns racesFlow
        every { observeTraits() } returns traitsFlow
        val viewModel = RacesViewModel(observeRaces, observeTraits)
        advanceUntilIdle()

        // When
        viewModel.onRaceClick(race)
        advanceUntilIdle()
        viewModel.uiState.first { it is RacesUiState.Content && it.selectedItemId == race.id }
        viewModel.onRaceClick(race)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first {
            it is RacesUiState.Content && it.selectedItemId == null
        } as RacesUiState.Content
        assertThat(state.selectedItemId).isNull()
    }
}
