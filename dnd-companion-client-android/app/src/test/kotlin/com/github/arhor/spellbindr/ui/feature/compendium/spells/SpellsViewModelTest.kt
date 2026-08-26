package com.github.arhor.spellbindr.ui.feature.compendium.spells

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.GenericInfo
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.Spellcasting
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private typealias ObserveSpellsArgs = Triple<String, Set<EntityRef>, Boolean>

class SpellsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSpells = mockk<ObserveSpellsUseCase>()
    private val observeSpellcastingClasses = mockk<ObserveSpellcastingClassesUseCase>()

    private val wizard = EntityRef("wizard")
    private val cleric = EntityRef("cleric")
    private val favoriteSpellIds = setOf("magic_missile")

    private val spells = listOf(
        buildSpell(
            id = "magic_missile",
            name = "Magic Missile",
            classes = listOf(wizard),
        ),
        buildSpell(
            id = "healing_word",
            name = "Healing Word",
            classes = listOf(cleric),
        ),
        buildSpell(
            id = "cure_wounds",
            name = "Cure Wounds",
            classes = listOf(wizard, cleric),
        ),
    )

    @Test
    fun `uiState should emit loading then content when spells and classes are available`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val spellsFlow = MutableStateFlow<Loadable<List<Spell>>>(Loadable.Loading)
            val classesFlow = MutableStateFlow<Loadable<List<CharacterClass>>>(Loadable.Loading)
            every { observeSpells(any(), any(), any()) } returns spellsFlow
            every { observeSpellcastingClasses() } returns classesFlow
            val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
            val states = mutableListOf<SpellsUiState>()

            // When
            val job = launch { viewModel.uiState.take(2).toList(states) }
            spellsFlow.value = Loadable.Content(spells)
            classesFlow.value = Loadable.Content(
                listOf(
                    buildSpellcastingClass("wizard"),
                    buildSpellcastingClass("cleric"),
                )
            )
            advanceUntilIdle()
            job.join()

            // Then
            assertThat(states)
                .containsExactly(
                    SpellsUiState.Loading,
                    SpellsUiState.Content(
                        query = "",
                        spells = spells,
                        showFavoriteOnly = false,
                        castingClasses = listOf(wizard, cleric),
                        selectedClasses = emptySet(),
                    ),
                )
                .inOrder()

            verify(exactly = 1) { observeSpells("", emptySet(), false) }
            verify(exactly = 1) { observeSpellcastingClasses() }
        }

    @Test
    fun `uiState should emit failure when spells use case fails`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val spellsFlow = MutableStateFlow<Loadable<List<Spell>>>(Loadable.Loading)
        val classesFlow = MutableStateFlow<Loadable<List<CharacterClass>>>(
            Loadable.Content(listOf(buildSpellcastingClass("wizard"))),
        )
        every { observeSpells(any(), any(), any()) } returns spellsFlow
        every { observeSpellcastingClasses() } returns classesFlow
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)

        // When
        val errorState = async { viewModel.uiState.first { it is SpellsUiState.Failure } }
        spellsFlow.value = Loadable.Failure()

        advanceUntilIdle()

        // Then
        assertThat(errorState.await()).isEqualTo(SpellsUiState.Failure("Failed to load spells."))
    }

    @Test
    fun `uiState should emit failure when spellcasting classes use case fails`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val spellsFlow = MutableStateFlow<Loadable<List<Spell>>>(Loadable.Content(spells))
            val classesFlow = MutableStateFlow<Loadable<List<CharacterClass>>>(Loadable.Loading)
            every { observeSpells(any(), any(), any()) } returns spellsFlow
            every { observeSpellcastingClasses() } returns classesFlow
            val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)

            // When
            val errorState = async { viewModel.uiState.first { it is SpellsUiState.Failure } }
            classesFlow.value = Loadable.Failure(errorMessage = "Classes failed")
            advanceUntilIdle()

            // Then
            assertThat(errorState.await()).isEqualTo(
                SpellsUiState.Failure("Failed to load spellcasting classes."),
            )
        }

    @Test
    fun `dispatch should debounce non blank query when user types`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val calls = mutableListOf<ObserveSpellsArgs>()

        every { observeSpells(any(), any(), any()) } answers {
            val query = firstArg<String>()
            val classes = secondArg<Set<EntityRef>>()
            val favoritesOnly = thirdArg<Boolean>()
            calls.add(Triple(query, classes, favoritesOnly))
            flowOf(Loadable.Content(emptyList()))
        }
        every { observeSpellcastingClasses() } returns flowOf(
            Loadable.Content(listOf(buildSpellcastingClass("wizard"))),
        )
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        val job = launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        // When
        viewModel.dispatch(SpellsIntent.QueryChanged("  Heal  "))

        advanceTimeBy(349)
        runCurrent()

        val callsBeforeDebounce = calls.toList()

        advanceTimeBy(1)
        runCurrent()

        // Then
        assertThat(callsBeforeDebounce.any { it.first == "Heal" }).isFalse()
        assertThat(calls).contains(ObserveSpellsArgs("", emptySet(), false))
        assertThat(calls.last()).isEqualTo(ObserveSpellsArgs("Heal", emptySet(), false))

        job.cancel()
    }

    @Test
    fun `dispatch should emit immediately when query cleared`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val calls = mutableListOf<Triple<String, Set<EntityRef>, Boolean>>()
        val expectedArgs = listOf(
            ObserveSpellsArgs("", emptySet(), false),
            ObserveSpellsArgs("Heal", emptySet(), false),
            ObserveSpellsArgs("", emptySet(), false),
        )

        every { observeSpells(any(), any(), any()) } answers {
            val query = firstArg<String>()
            val classes = secondArg<Set<EntityRef>>()
            val favoritesOnly = thirdArg<Boolean>()
            calls.add(Triple(query, classes, favoritesOnly))
            flowOf(Loadable.Content(emptyList()))
        }
        every { observeSpellcastingClasses() } returns flowOf(
            Loadable.Content(listOf(buildSpellcastingClass("wizard"))),
        )
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        // When
        viewModel.dispatch(SpellsIntent.QueryChanged("Heal"))
        advanceTimeBy(350)
        advanceUntilIdle()
        viewModel.dispatch(SpellsIntent.QueryChanged("   "))
        advanceUntilIdle()

        // Then
        assertThat(calls)
            .containsExactlyElementsIn(expectedArgs)
            .inOrder()

        job.cancel()
    }

    @Test
    fun `dispatch should show favorites only when favorites toggled`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            every { observeSpells(any(), any(), any()) } answers {
                val query = firstArg<String>()
                val classes = secondArg<Set<EntityRef>>()
                val favoritesOnly = thirdArg<Boolean>()
                flowOf(Loadable.Content(filterSpells(query, classes, favoritesOnly)))
            }
            every { observeSpellcastingClasses() } returns flowOf(
                Loadable.Content(
                    listOf(
                        buildSpellcastingClass("wizard"),
                        buildSpellcastingClass("cleric"),
                    )
                ),
            )
            val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
            advanceUntilIdle()

            // When
            viewModel.dispatch(SpellsIntent.FavoritesToggled)
            advanceUntilIdle()

            // Then
            val content = viewModel.uiState.first {
                it is SpellsUiState.Content && it.showFavoriteOnly
            } as SpellsUiState.Content
            assertThat(content.spells).containsExactly(spells[0]).inOrder()
            assertThat(content.showFavoriteOnly).isTrue()
            verify { observeSpells("", emptySet(), true) }
        }

    @Test
    fun `dispatch should update selected classes when class filter toggled`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            every { observeSpells(any(), any(), any()) } answers {
                val query = firstArg<String>()
                val classes = secondArg<Set<EntityRef>>()
                val favoritesOnly = thirdArg<Boolean>()
                flowOf(Loadable.Content(filterSpells(query, classes, favoritesOnly)))
            }
            every { observeSpellcastingClasses() } returns flowOf(
                Loadable.Content(
                    listOf(
                        buildSpellcastingClass("wizard"),
                        buildSpellcastingClass("cleric"),
                    )
                ),
            )
            val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
            advanceUntilIdle()

            // When
            viewModel.dispatch(SpellsIntent.ClassFilterToggled(wizard))
            advanceUntilIdle()
            val filteredState = viewModel.uiState.first {
                it is SpellsUiState.Content && it.selectedClasses == setOf(wizard)
            } as SpellsUiState.Content
            viewModel.dispatch(SpellsIntent.ClassFilterToggled(wizard))
            advanceUntilIdle()

            // Then
            assertThat(filteredState.spells).containsExactly(spells[0], spells[2]).inOrder()
            assertThat(filteredState.selectedClasses).containsExactly(wizard)
            val clearedState = viewModel.uiState.first {
                it is SpellsUiState.Content && it.selectedClasses.isEmpty()
            } as SpellsUiState.Content
            assertThat(clearedState.spells).containsExactlyElementsIn(spells).inOrder()
            assertThat(clearedState.selectedClasses).isEmpty()
        }

    private fun filterSpells(
        query: String,
        classes: Set<EntityRef>,
        favoritesOnly: Boolean,
    ): List<Spell> {
        val normalizedQuery = query.trim()
        return spells.filter { spell ->
            val queryMatches = normalizedQuery.isEmpty() || spell.name.contains(normalizedQuery, ignoreCase = true)
            val classMatches = classes.isEmpty() || spell.classes.any { it in classes }
            val favoritesMatch = !favoritesOnly || spell.id in favoriteSpellIds
            queryMatches && classMatches && favoritesMatch
        }
    }

    private fun buildSpellcastingClass(id: String): CharacterClass = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercaseChar() },
        hitDie = 8,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        spellcasting = Spellcasting(
            info = emptyList<GenericInfo>(),
            level = 1,
            spellcastingAbility = EntityRef("intelligence"),
        ),
        startingEquipment = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )

    private fun buildSpell(
        id: String,
        name: String,
        classes: List<EntityRef>,
    ): Spell = Spell(
        id = id,
        name = name,
        desc = listOf("desc"),
        level = 1,
        range = "60 ft",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instant",
        castingTime = "1 action",
        classes = classes,
        components = listOf("V"),
        concentration = false,
        source = "PHB",
    )
}
