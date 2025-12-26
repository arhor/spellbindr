package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.FakeAlignmentRepository
import com.github.arhor.spellbindr.domain.repository.FakeCharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.FakeRacesRepository
import com.github.arhor.spellbindr.domain.repository.FakeTraitsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ReferenceDataUseCasesTest {

    private val alignmentRepository = FakeAlignmentRepository()
    private val racesRepository = FakeRacesRepository()
    private val traitsRepository = FakeTraitsRepository()
    private val characterClassRepository = FakeCharacterClassRepository()

    @Test
    fun `ObserveAlignmentsUseCase should emit latest alignments when repository updates`() = runTest {
        // Given
        val alignment = Alignment(id = "lawful-good", name = "Lawful Good", desc = "Desc", abbr = "LG")
        alignmentRepository.allAlignmentsState.value = listOf(alignment)

        // When
        val result = ObserveAlignmentsUseCase(alignmentRepository)().first()

        // Then
        assertThat(result).containsExactly(alignment)
    }

    @Test
    fun `ObserveRacesUseCase should emit latest races when repository updates`() = runTest {
        // Given
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef("keen-senses")),
            subraces = emptyList(),
        )
        racesRepository.allRacesState.value = listOf(race)

        // When
        val result = ObserveRacesUseCase(racesRepository)().first()

        // Then
        assertThat(result).containsExactly(race)
    }

    @Test
    fun `ObserveTraitsUseCase should emit latest traits when repository updates`() = runTest {
        // Given
        val trait = Trait(id = "darkvision", name = "Darkvision", desc = listOf("See in the dark."))
        traitsRepository.allTraitsState.value = listOf(trait)

        // When
        val result = ObserveTraitsUseCase(traitsRepository)().first()

        // Then
        assertThat(result).containsExactly(trait)
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
