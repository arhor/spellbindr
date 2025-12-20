package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.clearDeathSavesIfConscious
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterSheetDeathSavesLogicTest {

    @Test
    fun `clearDeathSavesIfConscious resets death saves when hp is positive`() {
        val sheet = CharacterSheet(
            id = "test",
            currentHitPoints = 5,
            deathSaves = DeathSaveState(successes = 2, failures = 1),
        )

        val result = sheet.clearDeathSavesIfConscious()

        assertThat(result.currentHitPoints).isEqualTo(5)
        assertThat(result.deathSaves.successes).isEqualTo(0)
        assertThat(result.deathSaves.failures).isEqualTo(0)
    }

    @Test
    fun `clearDeathSavesIfConscious keeps death saves when hp is zero`() {
        val sheet = CharacterSheet(
            id = "test",
            currentHitPoints = 0,
            deathSaves = DeathSaveState(successes = 1, failures = 2),
        )

        val result = sheet.clearDeathSavesIfConscious()

        assertThat(result.currentHitPoints).isEqualTo(0)
        assertThat(result.deathSaves.successes).isEqualTo(1)
        assertThat(result.deathSaves.failures).isEqualTo(2)
    }
}
