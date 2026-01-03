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
        racesRepository.allRacesState.value = Loadable.Ready(listOf(race))

        // When
        val result = ObserveRacesUseCase(racesRepository)().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Ready(listOf(race)))
    }

    @Test
    fun `ObserveTraitsUseCase should emit latest traits when repository updates`() = runTest {
        // Given
        val trait = Trait(id = "darkvision", name = "Darkvision", desc = listOf("See in the dark."))
        traitsRepository.allTraitsState.value = Loadable.Ready(listOf(trait))

        // When
        val result = ObserveTraitsUseCase(traitsRepository)().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Ready(listOf(trait)))
    }

    @Test
    fun `GetSpellcastingClassRefsUseCase should return repository refs when invoked`() = runTest {
        // Given
        val classes = listOf(EntityRef("wizard"), EntityRef("cleric"))
        characterClassRepository.spellcastingClassesRefs = classes

        // When
        val result = GetSpellcastingClassRefsUseCase(characterClassRepository)()

        // Then
        assertThat(result).containsExactlyElementsIn(classes).inOrder()
    }
}
