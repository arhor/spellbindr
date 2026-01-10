package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.FakeCharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.FakeRacesRepository
import com.github.arhor.spellbindr.domain.repository.FakeTraitsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ReferenceDataUseCasesTest {

    private val racesRepository = FakeRacesRepository()
    private val traitsRepository = FakeTraitsRepository()
    private val characterClassRepository = FakeCharacterClassRepository()

    @Test
    fun `ObserveRacesUseCase should emit latest races when repository updates`() = runTest {
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
    fun `ObserveTraitsUseCase should emit latest traits when repository updates`() = runTest {
        // Given
        val trait = Trait(id = "darkvision", name = "Darkvision", desc = listOf("See in the dark."))
        traitsRepository.allTraitsState.value = Loadable.Content(listOf(trait))

        // When
        val result = ObserveAllTraitsUseCase(traitsRepository)().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(trait)))
    }
}
