package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpellsUseCasesTest {

    private val repository = FakeSpellsRepository()

    @Test
    fun `observeAllSpells emits latest spells`() = runTest {
        // Given
        val spell = sampleSpell(id = "magic-missile", name = "Magic Missile")
        repository.allSpellsState.value = listOf(spell)

        // When
        val result = ObserveAllSpellsUseCase(repository)().first()

        // Then
        assertThat(result).containsExactly(spell)
    }

    @Test
    fun `observeFavoriteSpellIds emits latest favorites`() = runTest {
        // Given
        repository.favoriteSpellIdsState.value = listOf("fireball", "mage-armor")

        // When
        val result = ObserveFavoriteSpellIdsUseCase(repository)().first()

        // Then
        assertThat(result).containsExactly("fireball", "mage-armor").inOrder()
    }

    @Test
    fun `getSpellById returns matching spell`() = runTest {
        // Given
        val spell = sampleSpell(id = "shield", name = "Shield")
        repository.spellsById[spell.id] = spell

        // When
        val result = GetSpellByIdUseCase(repository)(spell.id)

        // Then
        assertThat(result).isEqualTo(spell)
    }

    @Test
    fun `getSpellById returns null for unknown spell`() = runTest {
        // When
        val result = GetSpellByIdUseCase(repository)("missing")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `searchSpells forwards filters to repository`() = runTest {
        // Given
        val classes = setOf(EntityRef("cleric"), EntityRef("wizard"))
        val expected = listOf(sampleSpell(id = "cure-wounds", name = "Cure Wounds"))
        repository.findSpellsResult = expected

        // When
        val result = SearchSpellsUseCase(repository)(
            query = "Cure",
            classes = classes,
            favoriteOnly = true,
        )

        // Then
        assertThat(result).isEqualTo(expected)
        assertThat(repository.lastFindSpellsQuery).isEqualTo("Cure")
        assertThat(repository.lastFindSpellsClasses).isEqualTo(classes)
        assertThat(repository.lastFindSpellsFavoriteOnly).isTrue()
    }

    @Test
    fun `toggleFavoriteSpell forwards id`() = runTest {
        // When
        ToggleFavoriteSpellUseCase(repository)("fireball")

        // Then
        assertThat(repository.lastToggledSpellId).isEqualTo("fireball")
    }

    @Test
    fun `isSpellFavorite returns false for null id`() = runTest {
        // When
        val result = IsSpellFavoriteUseCase(repository)(null)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `isSpellFavorite returns repository value`() = runTest {
        // Given
        repository.isFavoriteResult = true

        // When
        val result = IsSpellFavoriteUseCase(repository)("fireball")

        // Then
        assertThat(result).isTrue()
        assertThat(repository.lastIsFavoriteSpellId).isEqualTo("fireball")
    }

    private fun sampleSpell(
        id: String,
        name: String,
    ): Spell = Spell(
        id = id,
        name = name,
        desc = listOf("A sample spell."),
        level = 1,
        range = "Self",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = listOf(EntityRef("wizard")),
        components = listOf("V", "S"),
        concentration = false,
        source = "SRD",
    )

    private class FakeSpellsRepository : SpellsRepository {
        val allSpellsState = MutableStateFlow<List<Spell>>(emptyList())
        val favoriteSpellIdsState = MutableStateFlow<List<String>>(emptyList())
        val spellsById = mutableMapOf<String, Spell>()

        var findSpellsResult: List<Spell> = emptyList()
        var lastFindSpellsQuery: String? = null
        var lastFindSpellsClasses: Set<EntityRef>? = null
        var lastFindSpellsFavoriteOnly: Boolean? = null

        var lastToggledSpellId: String? = null
        var lastIsFavoriteSpellId: String? = null
        var isFavoriteResult: Boolean = false

        override val allSpells: Flow<List<Spell>> = allSpellsState
        override val favoriteSpellIds: Flow<List<String>> = favoriteSpellIdsState

        override suspend fun getSpellById(id: String): Spell? = spellsById[id]

        override suspend fun findSpells(
            query: String,
            classes: Set<EntityRef>,
            favoriteOnly: Boolean,
        ): List<Spell> {
            lastFindSpellsQuery = query
            lastFindSpellsClasses = classes
            lastFindSpellsFavoriteOnly = favoriteOnly
            return findSpellsResult
        }

        override suspend fun toggleFavorite(spellId: String) {
            lastToggledSpellId = spellId
        }

        override suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>?): Boolean {
            lastIsFavoriteSpellId = spellId
            return spellId != null && isFavoriteResult
        }
    }
}
