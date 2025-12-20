package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpellsUseCasesTest {

    private val repository = FakeSpellsRepository()
    private val favoritesRepository = FakeFavoritesRepository()

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
        favoritesRepository.favoriteIdsState.value = listOf("fireball", "mage-armor")

        // When
        val result = ObserveFavoriteSpellIdsUseCase(favoritesRepository)().first()

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
        favoritesRepository.favoriteIdsState.value = listOf("cure-wounds")

        // When
        val result = SearchSpellsUseCase(repository, favoritesRepository)(
            query = "Cure",
            classes = classes,
            favoriteOnly = true,
        )

        // Then
        assertThat(result).isEqualTo(expected)
        assertThat(repository.lastFindSpellsQuery).isEqualTo("Cure")
        assertThat(repository.lastFindSpellsClasses).isEqualTo(classes)
        assertThat(favoritesRepository.lastObservedType).isEqualTo(FavoriteType.SPELL)
    }

    @Test
    fun `searchSpells skips favorites when not requested`() = runTest {
        // Given
        val expected = listOf(sampleSpell(id = "detect-magic", name = "Detect Magic"))
        repository.findSpellsResult = expected

        // When
        val result = SearchSpellsUseCase(repository, favoritesRepository)(
            query = "Detect",
            classes = emptySet(),
            favoriteOnly = false,
        )

        // Then
        assertThat(result).isEqualTo(expected)
        assertThat(favoritesRepository.lastObservedType).isNull()
    }

    @Test
    fun `toggleFavoriteSpell forwards id`() = runTest {
        // When
        ToggleFavoriteSpellUseCase(favoritesRepository)("fireball")

        // Then
        assertThat(favoritesRepository.lastToggledSpellId).isEqualTo("fireball")
        assertThat(favoritesRepository.lastToggledType).isEqualTo(FavoriteType.SPELL)
    }

    @Test
    fun `isSpellFavorite returns false for null id`() = runTest {
        // When
        val result = IsSpellFavoriteUseCase(favoritesRepository)(null)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `isSpellFavorite returns repository value`() = runTest {
        // Given
        favoritesRepository.isFavoriteResult = true

        // When
        val result = IsSpellFavoriteUseCase(favoritesRepository)("fireball")

        // Then
        assertThat(result).isTrue()
        assertThat(favoritesRepository.lastIsFavoriteSpellId).isEqualTo("fireball")
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

    @Suppress("DEPRECATION")
    private class FakeSpellsRepository : SpellsRepository {
        val allSpellsState = MutableStateFlow<List<Spell>>(emptyList())
        val spellsById = mutableMapOf<String, Spell>()

        var findSpellsResult: List<Spell> = emptyList()
        var lastFindSpellsQuery: String? = null
        var lastFindSpellsClasses: Set<EntityRef>? = null

        override val allSpells: Flow<List<Spell>> = allSpellsState
        override suspend fun getSpellById(id: String): Spell? = spellsById[id]

        override suspend fun findSpells(
            query: String,
            classes: Set<EntityRef>,
        ): List<Spell> {
            lastFindSpellsQuery = query
            lastFindSpellsClasses = classes
            return findSpellsResult
        }

        override suspend fun toggleFavorite(spellId: String) = Unit

        override suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>?): Boolean = false
    }

    private class FakeFavoritesRepository : FavoritesRepository {
        val favoriteIdsState = MutableStateFlow<List<String>>(emptyList())
        var lastObservedType: FavoriteType? = null
        var lastToggledType: FavoriteType? = null
        var lastToggledSpellId: String? = null
        var lastIsFavoriteSpellId: String? = null
        var isFavoriteResult: Boolean = false

        override fun observeFavoriteIds(type: FavoriteType): Flow<List<String>> {
            lastObservedType = type
            return favoriteIdsState
        }

        override suspend fun toggleFavorite(type: FavoriteType, entityId: String) {
            lastToggledType = type
            lastToggledSpellId = entityId
        }

        override suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean {
            lastIsFavoriteSpellId = entityId
            return isFavoriteResult
        }
    }
}
