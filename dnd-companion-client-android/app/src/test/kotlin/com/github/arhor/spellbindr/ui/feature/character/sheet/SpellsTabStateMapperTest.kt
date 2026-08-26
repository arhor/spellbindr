package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spellcasting
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpellsTabStateMapperTest {

    @Test
    fun `toSpellsState should mark shared slots unconfigured when totals are zero`() {
        // Given
        val sheet = CharacterSheet(id = "test")

        // When
        val state = sheet.toSpellsState(allSpells = emptyList(), spellcastingClasses = emptyList())

        // Then
        assertThat(state.hasConfiguredSharedSlots).isFalse()
    }

    @Test
    fun `toSpellsState should expose unconfigured pact slots when warlock spells exist`() {
        // Given
        val sheet = CharacterSheet(
            id = "test",
            characterSpells = listOf(CharacterSpell(spellId = "eldritch-blast", sourceClass = "Warlock")),
            pactSlots = null,
        )
        val warlockClass = CharacterClass(
            id = "warlock",
            name = "Warlock",
            hitDie = 8,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = Spellcasting(
                info = emptyList(),
                level = 1,
                spellcastingAbility = EntityRef("cha"),
            ),
            startingEquipment = null,
            subclasses = emptyList(),
            levels = emptyList(),
        )

        // When
        val state = sheet.toSpellsState(allSpells = emptyList(), spellcastingClasses = listOf(warlockClass))

        // Then
        assertThat(state.pactSlots).isNotNull()
        assertThat(state.pactSlots?.isConfigured).isFalse()
    }
}
