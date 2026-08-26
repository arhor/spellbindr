package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Race
import io.github.arhor.dnd.companion.domain.model.Trait
import io.github.arhor.dnd.companion.domain.repository.FakeRacesRepository
import io.github.arhor.dnd.companion.domain.repository.FakeTraitsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ReferenceDataUseCasesTest {

    private val racesRepository = FakeRacesRepository()
    private val traitsRepository = FakeTraitsRepository()

    @Test
    fun `invoke should emit latest races when race repository updates`() = runTest {
        // Given
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef("keen-senses")),
            subraces = emptyList(),
        )
        racesRepository.allRacesState.value = Loadable.Content(listOf(race))

        // When
        val result = ObserveAllRacesUseCase(racesRepository)().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(race)))
    }

    @Test
    fun `invoke should emit latest traits when trait repository updates`() = runTest {
        // Given
        val trait = Trait(id = "darkvision", name = "Darkvision", desc = listOf("See in the dark."))
        traitsRepository.allTraitsState.value = Loadable.Content(listOf(trait))

        // When
        val result = ObserveAllTraitsUseCase(traitsRepository)().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(trait)))
    }
}
