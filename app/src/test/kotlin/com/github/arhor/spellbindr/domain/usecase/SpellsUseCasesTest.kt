package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellDetails
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpellsUseCasesTest {

    private val repository = FakeSpellsRepository()
    private val favoritesRepository = FakeFavoritesRepository()

    @Test
    fun `ObserveAllSpellsUseCase should emit latest spells when repository updates`() = runTest {
        // Given
        val spell = sampleSpell(id = "magic-missile", name = "Magic Missile")
        repository.allSpellsState.value = Loadable.Ready(listOf(spell))

        // When
        val result = ObserveAllSpellsUseCase(repository)().first()

        // Then
        assertThat(result).containsExactly(spell)
    }

    @Test
    fun `ObserveAllSpellsStateUseCase should emit latest state when repository updates`() = runTest {
        // Given
        val spell = sampleSpell(id = "shield", name = "Shield")
        val state = Loadable.Ready(listOf(spell))
        repository.allSpellsState.value = state

        // When
        val result = ObserveAllSpellsStateUseCase(repository)().first()

        // Then
        assertThat(result).isEqualTo(state)
    }

    @Test
    fun `ObserveFavoriteSpellIdsUseCase should emit latest favorites when repository updates`() = runTest {
        // Given
        favoritesRepository.favoriteIdsState.value = listOf("fireball", "mage-armor")

        // When
        val result = ObserveFavoriteSpellIdsUseCase(favoritesRepository)().first()

        // Then
        assertThat(result).containsExactly("fireball", "mage-armor").inOrder()
    }

    @Test
    fun `SearchSpellsUseCase should return matching favorites when query and filter apply`() = runTest {
        // Given
        val classes = setOf(EntityRef("cleric"), EntityRef("wizard"))
        val expected = listOf(sampleSpell(id = "cure-wounds", name = "Cure Wounds", classes = classes.toList()))
        repository.allSpellsState.value = Loadable.Ready(expected)
        favoritesRepository.favoriteIdsState.value = listOf("cure-wounds")

        // When
        val result = SearchSpellsUseCase(SearchAndGroupSpellsUseCase(repository, favoritesRepository))(
            query = "Cure",
            classes = classes,
            favoriteOnly = true,
        )

        // Then
        assertThat(result).isEqualTo(expected)
        assertThat(favoritesRepository.lastObservedType).isEqualTo(FavoriteType.SPELL)
    }

    @Test
    fun `SearchSpellsUseCase should skip favorites when favoriteOnly is false`() = runTest {
        // Given
        val expected = listOf(sampleSpell(id = "detect-magic", name = "Detect Magic"))
        repository.allSpellsState.value = Loadable.Ready(expected)

        // When
        val result = SearchSpellsUseCase(SearchAndGroupSpellsUseCase(repository, favoritesRepository))(
            query = "Detect",
            classes = emptySet(),
            favoriteOnly = false,
        )

        // Then
        assertThat(result).isEqualTo(expected)
        assertThat(favoritesRepository.lastObservedType).isNull()
    }

    @Test
    fun `SearchAndGroupSpellsUseCase should filter by query class and favorites when parameters are provided`() = runTest {
        // Given
        val wizard = EntityRef("wizard")
        val cleric = EntityRef("cleric")
        val magicMissile = sampleSpell(id = "magic-missile", name = "Magic Missile", classes = listOf(wizard))
        val fireball = sampleSpell(
            id = "fireball",
            name = "Fireball",
            classes = listOf(wizard),
            level = 3,
        )
        val cureWounds = sampleSpell(id = "cure-wounds", name = "Cure Wounds", classes = listOf(cleric))
        repository.allSpellsState.value = Loadable.Ready(listOf(magicMissile, fireball, cureWounds))
        favoritesRepository.favoriteIdsState.value = listOf("fireball")

        // When
        val result = SearchAndGroupSpellsUseCase(repository, favoritesRepository)(
            query = "Fire",
            classes = setOf(wizard),
            favoriteOnly = true,
        )

        // Then
        assertThat(result.spells).containsExactly(fireball)
        assertThat(result.spellsByLevel.keys).containsExactly(3)
        assertThat(result.totalCount).isEqualTo(1)
        assertThat(result.query).isEqualTo("Fire")
        assertThat(result.classes).containsExactly(wizard)
    }

    @Test
    fun `ToggleFavoriteSpellUseCase should forward id when toggling favorite`() = runTest {
        // Given
        val spellId = "fireball"

        // When
        ToggleFavoriteSpellUseCase(favoritesRepository)(spellId)

        // Then
        assertThat(favoritesRepository.lastToggledSpellId).isEqualTo("fireball")
        assertThat(favoritesRepository.lastToggledType).isEqualTo(FavoriteType.SPELL)
    }

    @Test
    fun `ObserveSpellDetailsUseCase should emit loading and favorite updates when spell is found`() = runTest {
        // Given
        val spell = sampleSpell(id = "haste", name = "Haste")
        repository.spellsById[spell.id] = spell
        val useCase = ObserveSpellDetailsUseCase(
            spellsRepository = repository,
            favoritesRepository = favoritesRepository,
        )

        // When
        val emissions = mutableListOf<Loadable<SpellDetails>>()
        val job = launch {
            useCase(spell.id).take(3).toList(emissions)
        }
        advanceUntilIdle()
        favoritesRepository.favoriteIdsState.value = listOf(spell.id)
        advanceUntilIdle()
        job.join()

        // Then
        assertThat(emissions[0]).isEqualTo(Loadable.Loading)
        assertThat(emissions[1]).isEqualTo(Loadable.Ready(SpellDetails(spell = spell, isFavorite = false)))
        assertThat(emissions[2]).isEqualTo(Loadable.Ready(SpellDetails(spell = spell, isFavorite = true)))
    }

    @Test
    fun `ObserveSpellDetailsUseCase should emit error when spell is missing`() = runTest {
        // Given
        val useCase = ObserveSpellDetailsUseCase(
            spellsRepository = repository,
            favoritesRepository = favoritesRepository,
        )

        // When
        val emissions = useCase("missing").take(2).toList()

        // Then
        assertThat(emissions[0]).isEqualTo(Loadable.Loading)
        assertThat(emissions[1]).isEqualTo(Loadable.Error("Spell not found."))
    }

    @Test
    fun `ObserveSpellDetailsUseCase should emit error when repository throws`() = runTest {
        // Given
        repository.throwOnGetSpellById = true
        val useCase = ObserveSpellDetailsUseCase(
            spellsRepository = repository,
            favoritesRepository = favoritesRepository,
        )

        // When
        val emissions = useCase("fail").take(2).toList()

        // Then
        assertThat(emissions[0]).isEqualTo(Loadable.Loading)
        val errorState = emissions[1] as Loadable.Error
        assertThat(errorState.errorMessage).isEqualTo("Oops, something went wrong...")
    }

    private fun sampleSpell(
        id: String,
        name: String,
        level: Int = 1,
        classes: List<EntityRef> = listOf(EntityRef("wizard")),
    ): Spell = Spell(
        id = id,
        name = name,
        desc = listOf("A sample spell."),
        level = level,
        range = "Self",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = classes,
        components = listOf("V", "S"),
        concentration = false,
        source = "SRD",
    )

    private class FakeSpellsRepository : SpellsRepository {
        override val allSpellsState =
            MutableStateFlow<Loadable<List<Spell>>>(Loadable.Ready(emptyList()))
        val favoriteSpellIdsState = MutableStateFlow<List<String>>(emptyList())
        val spellsById = mutableMapOf<String, Spell>()

        var throwOnGetSpellById: Boolean = false
        var lastToggledSpellId: String? = null
        var lastIsFavoriteSpellId: String? = null

        override val favoriteSpellIds: Flow<List<String>> = favoriteSpellIdsState

        override suspend fun getSpellById(id: String): Spell? {
            if (throwOnGetSpellById) {
                throw IllegalStateException("Failed to load spell")
            }
            return spellsById[id]
        }

        override suspend fun toggleFavorite(spellId: String) {
            lastToggledSpellId = spellId
            val current = favoriteSpellIdsState.value.toMutableList()
            if (current.contains(spellId)) {
                current.remove(spellId)
            } else {
                current.add(spellId)
            }
            favoriteSpellIdsState.value = current
        }

        override suspend fun isFavorite(
            spellId: String?,
            favoriteSpellIds: List<String>?,
        ): Boolean {
            lastIsFavoriteSpellId = spellId
            if (spellId == null) {
                return false
            }
            val ids = favoriteSpellIds ?: favoriteSpellIdsState.value
            return ids.contains(spellId)
        }
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
