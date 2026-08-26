package com.github.arhor.spellbindr.ui.feature.character

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.ui.feature.character.sheet.clearDeathSavesIfConscious
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterSheetDeathSavesLogicTest {

    @Test
    fun `clearDeathSavesIfConscious should reset death saves when hp is positive`() {
        // Given
        val sheet = CharacterSheet(
            id = "test",
            currentHitPoints = 5,
            deathSaves = DeathSaveState(successes = 2, failures = 1),
        )

        // When
        val result = sheet.clearDeathSavesIfConscious()

        // Then
        assertThat(result.currentHitPoints).isEqualTo(5)
        assertThat(result.deathSaves.successes).isEqualTo(0)
        assertThat(result.deathSaves.failures).isEqualTo(0)
    }

    @Test
    fun `clearDeathSavesIfConscious should keep death saves when hp is zero`() {
        // Given
        val sheet = CharacterSheet(
            id = "test",
            currentHitPoints = 0,
            deathSaves = DeathSaveState(successes = 1, failures = 2),
        )

        // When
        val result = sheet.clearDeathSavesIfConscious()

        // Then
        assertThat(result.currentHitPoints).isEqualTo(0)
        assertThat(result.deathSaves.successes).isEqualTo(1)
        assertThat(result.deathSaves.failures).isEqualTo(2)
    }
}
