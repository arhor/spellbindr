package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Alignment
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.AlignmentRepository
import io.github.arhor.dnd.companion.utils.unwrap
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveAlignmentsUseCaseTest {

    private val alignmentRepository = mockk<AlignmentRepository>()
    private val observeAllAlignmentsUseCase = ObserveAllAlignmentsUseCase(alignmentRepository)

    @Test
    fun `invoke should emit latest alignments when repository updates`() = runTest {
        // Given
        val alignment = Alignment(id = "lawful-good", name = "Lawful Good", desc = "Desc", abbr = "LG")
        val loadable = Loadable.Content(listOf(alignment))

        every { alignmentRepository.allAlignmentsState } returns flowOf(loadable)

        // When
        val result =
            observeAllAlignmentsUseCase()
                .unwrap()
                .first()

        // Then
        assertThat(result)
            .containsExactly(alignment)
    }
}
