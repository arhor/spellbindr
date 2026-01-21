package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllAlignmentsUseCase
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

class AlignmentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeAlignments = mockk<ObserveAllAlignmentsUseCase>()

    @Test
    fun `uiState should emit loading then content when alignments are available`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val alignment = Alignment(id = "lg", name = "Lawful Good", desc = "Desc", abbr = "LG")
            val alignmentsFlow = MutableStateFlow<Loadable<List<Alignment>>>(Loadable.Loading)
            every { observeAlignments() } returns alignmentsFlow
            val viewModel = AlignmentsViewModel(observeAlignments)

            // When
            val states = mutableListOf<AlignmentsUiState>()
            val job = launch {
                viewModel.uiState.take(2).toList(states)
            }
            alignmentsFlow.value = Loadable.Content(listOf(alignment))
            advanceUntilIdle()
            job.cancel()

            // Then
            assertThat(states)
                .containsExactly(
                    AlignmentsUiState.Loading,
                    AlignmentsUiState.Content(listOf(alignment), selectedItemId = null),
                )
                .inOrder()
            verify(exactly = 1) { observeAlignments() }
        }

    @Test
    fun `uiState should emit error message when use case fails without message`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val alignmentsFlow = MutableStateFlow<Loadable<List<Alignment>>>(Loadable.Loading)
            every { observeAlignments() } returns alignmentsFlow
            val viewModel = AlignmentsViewModel(observeAlignments)

            // When
            val errorState = async { viewModel.uiState.first { it is AlignmentsUiState.Failure } }
            alignmentsFlow.value = Loadable.Failure()
            advanceUntilIdle()

            // Then
            assertThat(errorState.await())
                .isEqualTo(AlignmentsUiState.Failure("Failed to load alignments"))
        }

    @Test
    fun `onAlignmentClick should select alignment when new alignment tapped`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val alignment = Alignment(id = "tn", name = "True Neutral", desc = "Desc", abbr = "TN")
            val alignmentsFlow = MutableStateFlow<Loadable<List<Alignment>>>(Loadable.Content(listOf(alignment)))
            every { observeAlignments() } returns alignmentsFlow
            val viewModel = AlignmentsViewModel(observeAlignments)

            // When
            val states = mutableListOf<AlignmentsUiState>()
            val job = launch { viewModel.uiState.take(3).toList(states) }
            advanceUntilIdle()
            viewModel.onAlignmentClick(alignment)
            advanceUntilIdle()
            job.cancel()

            // Then
            val selectedState = states.last() as AlignmentsUiState.Content
            assertThat(selectedState.selectedItemId).isEqualTo(alignment.id)
        }

    @Test
    fun `onAlignmentClick should clear selection when same alignment tapped again`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val alignment = Alignment(id = "ce", name = "Chaotic Evil", desc = "Desc", abbr = "CE")
            val alignmentsFlow = MutableStateFlow<Loadable<List<Alignment>>>(Loadable.Content(listOf(alignment)))
            every { observeAlignments() } returns alignmentsFlow
            val viewModel = AlignmentsViewModel(observeAlignments)

            // When
            val states = mutableListOf<AlignmentsUiState>()
            val job = launch { viewModel.uiState.take(4).toList(states) }
            advanceUntilIdle()
            viewModel.onAlignmentClick(alignment)
            advanceUntilIdle()
            viewModel.onAlignmentClick(alignment)
            advanceUntilIdle()
            job.cancel()

            // Then
            val selectedState = states.last() as AlignmentsUiState.Content
            assertThat(selectedState.selectedItemId).isNull()
        }
}
