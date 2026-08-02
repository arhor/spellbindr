package com.github.arhor.spellbindr.ui.feature.character.levelup

import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterLevelUpStepPlannerTest {

    @Test
    fun `characterLevelUpSteps should keep conditional steps in wizard order when requirements are unordered`() {
        // Given
        val requirements = listOf(
            LevelUpRequirement.SpellDecisions("wizard:2:spells", "wizard", 2, "spellbook", SpellChanges()),
            LevelUpRequirement.HitPoints(hitDie = 6, selectedGain = null),
            LevelUpRequirement.ClassSelection(
                eligibleClassIds = listOf("wizard"),
                selectedClassId = "wizard",
            ),
        )

        // When
        val result = characterLevelUpSteps(requirements)

        // Then
        assertThat(result).containsExactly(
            CharacterLevelUpStep.Class,
            CharacterLevelUpStep.HitPoints,
            CharacterLevelUpStep.Spells,
            CharacterLevelUpStep.Review,
        ).inOrder()
    }

    @Test
    fun `characterLevelUpSteps should omit conditional steps when plan has no requirements`() {
        // Given
        val requirements = emptyList<LevelUpRequirement>()

        // When
        val result = characterLevelUpSteps(requirements)

        // Then
        assertThat(result).containsExactly(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review).inOrder()
    }
}
