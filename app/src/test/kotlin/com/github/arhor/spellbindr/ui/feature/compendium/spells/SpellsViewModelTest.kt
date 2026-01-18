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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SpellsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSpells = mockk<ObserveSpellsUseCase>()
    private val observeSpellcastingClasses = mockk<ObserveSpellcastingClassesUseCase>()

    private val wizard = EntityRef("wizard")
    private val cleric = EntityRef("cleric")

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
    fun `selecting one class filters spells`() = runTest(mainDispatcherRule.dispatcher) {
        stubUseCases()
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        advanceUntilIdle()

        viewModel.onClassFilterToggled(wizard)
        advanceUntilIdle()

        val content = viewModel.uiState.first {
            it is SpellsUiState.Content && it.selectedClasses == setOf(wizard)
        } as SpellsUiState.Content
        assertThat(content.spells).containsExactly(spells[0], spells[2]).inOrder()
        assertThat(content.selectedClasses).containsExactly(wizard)
    }

    @Test
    fun `selecting two classes applies OR filtering`() = runTest(mainDispatcherRule.dispatcher) {
        stubUseCases()
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        advanceUntilIdle()

        viewModel.onClassFilterToggled(wizard)
        advanceUntilIdle()
        viewModel.onClassFilterToggled(cleric)
        advanceUntilIdle()

        val content = viewModel.uiState.first {
            it is SpellsUiState.Content && it.selectedClasses == setOf(wizard, cleric)
        } as SpellsUiState.Content
        assertThat(content.spells).containsExactlyElementsIn(spells).inOrder()
        assertThat(content.selectedClasses).containsExactly(wizard, cleric)
    }

    @Test
    fun `deselecting all classes removes class filter`() = runTest(mainDispatcherRule.dispatcher) {
        stubUseCases()
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        advanceUntilIdle()

        viewModel.onClassFilterToggled(wizard)
        advanceUntilIdle()
        viewModel.uiState.first { it is SpellsUiState.Content && it.selectedClasses.contains(wizard) }

        viewModel.onClassFilterToggled(wizard)
        advanceUntilIdle()

        val content = viewModel.uiState.first {
            it is SpellsUiState.Content && it.selectedClasses.isEmpty()
        } as SpellsUiState.Content
        assertThat(content.spells).containsExactlyElementsIn(spells).inOrder()
        assertThat(content.selectedClasses).isEmpty()
    }

    @Test
    fun `search and class filters combine`() = runTest(mainDispatcherRule.dispatcher) {
        stubUseCases()
        val viewModel = SpellsViewModel(observeSpells, observeSpellcastingClasses)
        advanceUntilIdle()

        viewModel.onClassFilterToggled(cleric)
        advanceUntilIdle()
        viewModel.onQueryChanged("heal")
        advanceTimeBy(350)
        advanceUntilIdle()

        val content = viewModel.uiState.first {
            it is SpellsUiState.Content && it.query == "heal" && it.selectedClasses == setOf(cleric)
        } as SpellsUiState.Content
        assertThat(content.spells).containsExactly(spells[1])
        assertThat(content.selectedClasses).containsExactly(cleric)
        assertThat(content.query).isEqualTo("heal")
    }

    private fun stubUseCases() {
        every { observeSpells(any(), any(), any()) } answers {
            val query = firstArg<String>()
            val classes = secondArg<Set<EntityRef>>()
            flowOf(Loadable.Content(filterSpells(query, classes)))
        }
        every { observeSpellcastingClasses() } returns flowOf(
            Loadable.Content(
                listOf(
                    buildSpellcastingClass("wizard"),
                    buildSpellcastingClass("cleric"),
                )
            )
        )
    }

    private fun filterSpells(query: String, classes: Set<EntityRef>): List<Spell> {
        val normalizedQuery = query.trim()
        return spells.filter { spell ->
            val queryMatches = normalizedQuery.isEmpty() || spell.name.contains(normalizedQuery, ignoreCase = true)
            val classMatches = classes.isEmpty() || spell.classes.any { it in classes }
            queryMatches && classMatches
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
