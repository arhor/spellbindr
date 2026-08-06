package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.HitDicePoolState
import com.github.arhor.spellbindr.domain.model.ManagedProgressionSheetState
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetEditingState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterSheetManagedOwnershipTest {

    @Test
    fun `toOverviewState should expose managed saving throw grant when manual flag is false`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            abilityScores = AbilityScores(strength = 14),
            proficiencyBonus = 3,
            savingThrows = listOf(SavingThrowEntry(AbilityIds.STR, bonus = 5, proficient = false)),
            managedProgression = ManagedProgressionSheetState(
                savingThrowAbilityIds = setOf(AbilityIds.STR),
            ),
        )

        // When
        val strength = sheet.toOverviewState().abilities.single { it.abilityId == AbilityIds.STR }

        // Then
        assertThat(strength.savingThrowProficient).isTrue()
        assertThat(strength.savingThrowBonus).isEqualTo(5)
        assertThat(sheet.savingThrows.single().proficient).isFalse()
    }

    @Test
    fun `toSkillsState should expose managed skill grant when manual flag is false`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            abilityScores = AbilityScores(strength = 14),
            proficiencyBonus = 3,
            skills = listOf(SkillEntry(Skill.ATHLETICS, bonus = 5, proficient = false)),
            managedProgression = ManagedProgressionSheetState(
                proficiencyIds = setOf("skill-athletics"),
            ),
        )

        // When
        val athletics = sheet.toSkillsState().skills.single { it.id == Skill.ATHLETICS }

        // Then
        assertThat(athletics.proficient).isTrue()
        assertThat(athletics.totalBonus).isEqualTo(5)
        assertThat(sheet.skills.single().proficient).isFalse()
    }

    @Test
    fun `toOverviewState should compute managed saving throw when manual entry is missing`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            abilityScores = AbilityScores(dexterity = 14),
            proficiencyBonus = 3,
            savingThrows = emptyList(),
            managedProgression = ManagedProgressionSheetState(
                savingThrowAbilityIds = setOf(AbilityIds.DEX),
            ),
        )

        // When
        val dexterity = sheet.toOverviewState().abilities.single { it.abilityId == AbilityIds.DEX }

        // Then
        assertThat(dexterity.savingThrowProficient).isTrue()
        assertThat(dexterity.savingThrowBonus).isEqualTo(5)
    }

    @Test
    fun `toSkillsState should compute managed skill when manual entry is missing`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            abilityScores = AbilityScores(dexterity = 14),
            proficiencyBonus = 3,
            skills = emptyList(),
            managedProgression = ManagedProgressionSheetState(
                proficiencyIds = setOf("skill-stealth"),
            ),
        )

        // When
        val stealth = sheet.toSkillsState().skills.single { it.id == Skill.STEALTH }

        // Then
        assertThat(stealth.proficient).isTrue()
        assertThat(stealth.totalBonus).isEqualTo(5)
    }

    @Test
    fun `toOverviewState should display managed hit dice when manual field is blank`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            hitDice = "",
            managedProgression = ManagedProgressionSheetState(
                hitDicePools = listOf(
                    HitDicePoolState(dieSize = 10, total = 2, expended = 1),
                    HitDicePoolState(dieSize = 6, total = 1),
                ),
            ),
        )

        // When
        val overview = sheet.toOverviewState()

        // Then
        assertThat(overview.hitDice).isEqualTo("1d6 + 2d10")
        assertThat(sheet.hitDice).isEmpty()
    }

    @Test
    fun `applyInlineEdits should preserve blank manual hit dice when managed pool is displayed`() {
        // Given
        val sheet = CharacterSheet(
            id = "managed",
            hitDice = "",
            managedProgression = ManagedProgressionSheetState(
                hitDicePools = listOf(HitDicePoolState(dieSize = 8, total = 2)),
            ),
        )
        val edits = CharacterSheetEditingState.fromSheet(sheet)

        // When
        val updated = sheet.applyInlineEdits(edits)

        // Then
        assertThat(edits.hitDice).isEqualTo("2d8")
        assertThat(updated.hitDice).isEmpty()
        assertThat(updated.managedProgression?.hitDicePools).isEqualTo(sheet.managedProgression?.hitDicePools)
    }
}
