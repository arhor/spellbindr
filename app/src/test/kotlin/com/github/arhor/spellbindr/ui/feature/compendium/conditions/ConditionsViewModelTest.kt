package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllConditionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConditionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeConditions = mockk<ObserveAllConditionsUseCase>()

    @Test
    fun `uiState should emit loading then content when conditions load`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val loadableState = MutableStateFlow<Loadable<List<Condition>>>(Loadable.Loading)
        val condition = Condition(
            id = "blinded",
            displayName = "Blinded",
            description = listOf("Cannot see"),
        )

        every { observeConditions() } returns loadableState

        val viewModel = ConditionsViewModel(observeConditions)
        val states = mutableListOf<ConditionsUiState>()

        // When
        val job = launch {
            viewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()

        loadableState.value = Loadable.Content(listOf(condition))
        advanceUntilIdle()
        job.join()

        // Then
        assertThat(states).hasSize(2)
        assertThat(states[0]).isEqualTo(ConditionsUiState.Loading)
        assertThat(states[1]).isEqualTo(ConditionsUiState.Content(listOf(condition), null))
        verify(exactly = 1) { observeConditions() }
    }

    @Test
    fun `uiState should emit error state when loadable fails without message`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val loadableState = MutableStateFlow<Loadable<List<Condition>>>(Loadable.Failure())

            every { observeConditions() } returns loadableState

            val viewModel = ConditionsViewModel(observeConditions)

            // When
            val state = viewModel.uiState.drop(1).first()

            // Then
            assertThat(state).isEqualTo(ConditionsUiState.Error("Failed to load conditions"))
        }

    @Test
    fun `onConditionClick should toggle selected item when same condition clicked`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val condition = Condition(
                id = "blinded",
                displayName = "Blinded",
                description = listOf("Cannot see"),
            )
            val loadableState = MutableStateFlow<Loadable<List<Condition>>>(Loadable.Content(listOf(condition)))

            every { observeConditions() } returns loadableState

            val viewModel = ConditionsViewModel(observeConditions)
            val states = mutableListOf<ConditionsUiState>()

            // When
            val job = launch {
                viewModel.uiState.take(4).toList(states)
            }
            advanceUntilIdle()

            viewModel.onConditionClick(condition)
            advanceUntilIdle()

            viewModel.onConditionClick(condition)
            advanceUntilIdle()
            job.join()

            // Then
            assertThat(states).hasSize(4)
            assertThat(states[1]).isEqualTo(ConditionsUiState.Content(listOf(condition), null))
            assertThat(states[2]).isEqualTo(ConditionsUiState.Content(listOf(condition), condition.id))
            assertThat(states[3]).isEqualTo(ConditionsUiState.Content(listOf(condition), null))
        }
}
