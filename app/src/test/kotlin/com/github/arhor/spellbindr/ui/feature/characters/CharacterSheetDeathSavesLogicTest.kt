package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.DeathSaveState
import org.junit.Assert.assertEquals
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

        assertEquals(5, result.currentHitPoints)
        assertEquals(0, result.deathSaves.successes)
        assertEquals(0, result.deathSaves.failures)
    }

    @Test
    fun `clearDeathSavesIfConscious keeps death saves when hp is zero`() {
        val sheet = CharacterSheet(
            id = "test",
            currentHitPoints = 0,
            deathSaves = DeathSaveState(successes = 1, failures = 2),
        )

        val result = sheet.clearDeathSavesIfConscious()

        assertEquals(0, result.currentHitPoints)
        assertEquals(1, result.deathSaves.successes)
        assertEquals(2, result.deathSaves.failures)
    }
}

