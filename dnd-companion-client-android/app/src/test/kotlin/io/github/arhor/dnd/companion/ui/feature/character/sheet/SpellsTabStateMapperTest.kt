package io.github.arhor.dnd.companion.ui.feature.character.sheet

import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.CharacterSpell
import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.Spellcasting
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
