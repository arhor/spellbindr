package io.github.arhor.dnd.companion.ui.feature.character.levelup

import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.ClassSpellRef
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpPreview
import io.github.arhor.dnd.companion.domain.model.LevelUpRequirement
import io.github.arhor.dnd.companion.domain.model.LevelUpSelections
import io.github.arhor.dnd.companion.domain.model.LevelUpSnapshot
import io.github.arhor.dnd.companion.domain.model.LevelUpSpellOption
import io.github.arhor.dnd.companion.domain.model.SpellChanges
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterLevelUpSpellUiStateTest {

    @Test
    fun `canAdvance should be true when exact spell selections are complete`() {
        // Given
        val changes = SpellChanges(learned = setOf(ClassSpellRef("bard", "healing-word")))
        val requirement = LevelUpRequirement.SpellDecisions(
            id = "bard:2:spells",
            classId = "bard",
            classLevel = 2,
            policyId = "known",
            changes = changes,
            requiredKnownSpellCount = 1,
            knownSpellCandidates = listOf(LevelUpSpellOption("healing-word", "Healing Word", 1)),
        )
        val snapshot = LevelUpSnapshot(
            totalLevel = 1,
            classLevels = mapOf("bard" to 1),
            classDisplayName = "Bard 1",
            proficiencyBonus = 2,
            abilityScores = AbilityScores(),
            maximumHitPoints = 8,
            hitDicePools = emptyList(),
            proficiencyIds = emptySet(),
            savingThrowAbilityIds = emptySet(),
            featureIds = emptySet(),
            sharedCasterLevel = 1,
            sharedSpellSlots = mapOf(1 to 2),
        )
        val state = CharacterLevelUpUiState.Content(
            characterName = "Hero",
            plan = LevelUpPlan(
                expectedTotalLevel = 1,
                rulesetId = "srd-5e-2014-v1",
                referenceDataVersion = "test-v1",
                selectedClassId = "bard",
                selections = LevelUpSelections(spellChanges = changes),
            ),
            preview = LevelUpPreview(snapshot, snapshot.copy(totalLevel = 2), listOf(requirement), emptyList()),
            classes = emptyList(),
            feats = emptyList(),
            spells = emptyList(),
            steps = listOf(CharacterLevelUpStep.Spells, CharacterLevelUpStep.Review),
            step = CharacterLevelUpStep.Spells,
            currentStepIndex = 0,
        )

        // When
        val canAdvance = state.canAdvance

        // Then
        assertThat(canAdvance).isTrue()
    }
}
