package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterProgressionTest {

    @Test
    fun `totalLevel should derive class totals while preserving level order when records span multiple classes`() {
        // Given
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                level(characterLevel = 1, classId = "fighter", classLevel = 1),
                level(characterLevel = 2, classId = "wizard", classLevel = 1),
                level(characterLevel = 3, classId = "fighter", classLevel = 2),
            ),
        )

        // When
        val totalLevel = progression.totalLevel
        val classLevels = progression.classLevels
        val classOrder = progression.levels.map { it.classId }

        // Then
        assertThat(totalLevel).isEqualTo(3)
        assertThat(classLevels).containsExactly("fighter", 2, "wizard", 1)
        assertThat(classOrder).containsExactly("fighter", "wizard", "fighter").inOrder()
    }

    @Test
    fun `rulesetId should use the supported ruleset when no ruleset is provided`() {
        // Given
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = listOf(level(1, "fighter", 1)),
        )

        // When
        val rulesetId = progression.rulesetId

        // Then
        assertThat(rulesetId).isEqualTo("srd-5e-2014-v1")
    }

    private fun level(
        characterLevel: Int,
        classId: String,
        classLevel: Int,
    ) = CharacterLevelRecord(
        characterLevel = characterLevel,
        classId = classId,
        classLevel = classLevel,
        hitPointGain = HitPointGain.Fixed(6),
    )
}
