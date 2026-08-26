package com.github.arhor.spellbindr.ui.feature.character.levelup

import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class LevelUpSpellDraftSerializationTest {

    @Test
    fun `applySpellChangesSelection should restore spell selections when SavedState draft is decoded`() {
        // Given
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "healing-word")),
            featureLearned = mapOf(
                "magical-secrets-1" to setOf(ClassSpellRef("bard", "fire-bolt")),
            ),
            replacementSourceSpellId = "dissonant-whispers",
        )
        val plan = LevelUpPlan(
            expectedTotalLevel = 9,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = "test-v1",
            selectedClassId = "bard",
            selections = LevelUpSelections(),
        )

        // When
        val updated = plan.applySpellChangesSelection(CharacterLevelUpIntent.SpellChangesSelected(changes))
        val restored = Json.decodeFromString(
            LevelUpPlan.serializer(),
            Json.encodeToString(LevelUpPlan.serializer(), updated),
        )

        // Then
        assertThat(restored.selections.spellChanges).isEqualTo(changes)
    }
}
