package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.ManagedProgressionSheetState
import com.github.arhor.spellbindr.domain.model.Spellcasting
import com.github.arhor.spellbindr.domain.model.SpellcastingClassStats
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpellsTabSpellcastingStatsTest {

    @Test
    fun `toSpellsState should display materialized managed spellcasting stats`() {
        val wizard = wizardClass()
        val sheet = CharacterSheet(
            id = "character",
            proficiencyBonus = 2,
            abilityScores = AbilityScores(intelligence = 10),
            characterSpells = listOf(CharacterSpell("shield", "Wizard")),
            managedProgression = ManagedProgressionSheetState(
                spellcastingClassStats = mapOf(
                    "wizard" to SpellcastingClassStats(
                        abilityId = AbilityIds.INT,
                        spellSaveDc = 15,
                        spellAttackBonus = 7,
                    ),
                ),
            ),
        )

        val state = sheet.toSpellsState(allSpells = emptyList(), spellcastingClasses = listOf(wizard))

        val spellcasting = state.spellcastingClasses.single()
        assertThat(spellcasting.spellcastingAbility).isEqualTo("INT")
        assertThat(spellcasting.spellSaveDc).isEqualTo(15)
        assertThat(spellcasting.spellAttackBonus).isEqualTo(7)
    }

    @Test
    fun `toSpellsState should retain derived fallback for unmanaged sheets`() {
        val wizard = wizardClass()
        val sheet = CharacterSheet(
            id = "character",
            proficiencyBonus = 3,
            abilityScores = AbilityScores(intelligence = 16),
            characterSpells = listOf(CharacterSpell("shield", "wizard")),
        )

        val state = sheet.toSpellsState(allSpells = emptyList(), spellcastingClasses = listOf(wizard))

        val spellcasting = state.spellcastingClasses.single()
        assertThat(spellcasting.spellcastingAbility).isEqualTo("INT")
        assertThat(spellcasting.spellSaveDc).isEqualTo(14)
        assertThat(spellcasting.spellAttackBonus).isEqualTo(6)
    }

    private fun wizardClass(): CharacterClass = CharacterClass(
        id = "wizard",
        name = "Wizard",
        hitDie = 6,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        spellcasting = Spellcasting(
            info = emptyList(),
            level = 1,
            spellcastingAbility = EntityRef(AbilityIds.INT),
        ),
        subclasses = emptyList(),
        levels = emptyList(),
    )
}
