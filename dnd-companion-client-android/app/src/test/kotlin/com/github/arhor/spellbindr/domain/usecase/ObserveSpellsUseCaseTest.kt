package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveSpellsUseCaseTest {

    private val spellsRepository = mockk<SpellsRepository>()
    private val favoritesRepository = mockk<FavoritesRepository>()
    private val useCase = ObserveSpellsUseCase(spellsRepository, favoritesRepository)

    @Test
    fun `invoke should filter spells by query classes and favorites when favorites only true`() = runTest {
        // Given
        val wizard = EntityRef("wizard")
        val sorcerer = EntityRef("sorcerer")
        val cleric = EntityRef("cleric")
        val fireball = spell(id = "fireball", name = "Fireball", classes = listOf(wizard, sorcerer))
        val cureWounds = spell(id = "cure-wounds", name = "Cure Wounds", classes = listOf(cleric))
        val spellState = Loadable.Content(listOf(fireball, cureWounds))

        every { spellsRepository.allSpellsState } returns flowOf(spellState)
        every { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) } returns flowOf(setOf("fireball"))

        // When
        val result = useCase(query = "fire", characterClasses = setOf(wizard), getFavoritesOnly = true).first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(fireball)))
        verify(exactly = 1) { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) }
    }

    @Test
    fun `invoke should ignore favorites filter when favorites only is false`() = runTest {
        // Given
        val wizard = EntityRef("wizard")
        val cleric = EntityRef("cleric")
        val fireball = spell(id = "fireball", name = "Fireball", classes = listOf(wizard))
        val cureWounds = spell(id = "cure-wounds", name = "Cure Wounds", classes = listOf(cleric))
        val spellState = Loadable.Content(listOf(fireball, cureWounds))

        every { spellsRepository.allSpellsState } returns flowOf(spellState)
        every { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) } returns flowOf(setOf("fireball"))

        // When
        val result = useCase(getFavoritesOnly = false).first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(fireball, cureWounds)))
    }

    @Test
    fun `invoke should trim query before filtering when query includes extra whitespace`() = runTest {
        // Given
        val wizard = EntityRef("wizard")
        val lightningBolt = spell(id = "lightning-bolt", name = "Lightning Bolt", classes = listOf(wizard))
        val spellState = Loadable.Content(listOf(lightningBolt))

        every { spellsRepository.allSpellsState } returns flowOf(spellState)
        every { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) } returns flowOf(emptySet())

        // When
        val result = useCase(query = "  bolt  ").first()

        // Then
        assertThat(result).isEqualTo(Loadable.Content(listOf(lightningBolt)))
    }

    @Test
    fun `invoke should keep loading state when repository is still loading`() = runTest {
        // Given
        every { spellsRepository.allSpellsState } returns flowOf(Loadable.Loading)
        every { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) } returns flowOf(setOf("fireball"))

        // When
        val result = useCase().first()

        // Then
        assertThat(result).isEqualTo(Loadable.Loading)
    }

    @Test
    fun `invoke should emit failure when upstream throws exception`() = runTest {
        // Given
        val exception = IllegalStateException("boom")

        every { spellsRepository.allSpellsState } returns flow { throw exception }
        every { favoritesRepository.observeFavoriteIds(FavoriteType.SPELL) } returns flowOf(emptySet())

        // When
        val result = useCase().first()

        // Then
        val failure = result as Loadable.Failure
        assertThat(failure.errorMessage).isEqualTo("Failed to load spells")
        assertThat(failure.cause).isInstanceOf(IllegalStateException::class.java)
        assertThat(failure.cause).hasMessageThat().isEqualTo(exception.message)
    }

    private fun spell(
        id: String,
        name: String,
        classes: List<EntityRef>,
    ): Spell =
        Spell(
            id = id,
            name = name,
            desc = listOf("A spell description"),
            level = 1,
            range = "Self",
            ritual = false,
            school = EntityRef("evocation"),
            duration = "Instantaneous",
            castingTime = "1 action",
            classes = classes,
            components = listOf("V"),
            concentration = false,
            source = "phb",
        )
}
