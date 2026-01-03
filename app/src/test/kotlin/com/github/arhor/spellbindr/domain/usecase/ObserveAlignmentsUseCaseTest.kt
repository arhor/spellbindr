package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import com.github.arhor.spellbindr.utils.unwrap
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveAlignmentsUseCaseTest {

    private val alignmentRepository = mockk<AlignmentRepository>()
    private val observeAlignmentsUseCase = ObserveAlignmentsUseCase(alignmentRepository)

    @Test
    fun `ObserveAlignmentsUseCase should emit latest alignments when repository updates`() = runTest {
        // Given
        val alignment = Alignment(id = "lawful-good", name = "Lawful Good", desc = "Desc", abbr = "LG")
        val loadable = Loadable.Ready(listOf(alignment))

        every { alignmentRepository.allAlignmentsState } returns flowOf(loadable)

        // When
        val result =
            observeAlignmentsUseCase()
                .unwrap()
                .first()

        // Then
        assertThat(result)
            .containsExactly(alignment)
    }
}
