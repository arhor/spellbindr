package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpScreen
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpStep
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpUiState

@PreviewTest
@Preview(widthDp = 360, heightDp = 900)
@Composable
fun LevelUp_Review_Martial_Screenshot() {
    ReviewScreenshot(
        classes = listOf(simpleClass("fighter", "Fighter")),
        before = snapshot(4, mapOf("fighter" to 4), "Fighter 4"),
        after = snapshot(5, mapOf("fighter" to 5), "Fighter 5", proficiencyIds = setOf("weapon-martial"), featureIds = setOf("extra-attack")),
        features = listOf(Feature("extra-attack", "Extra Attack", listOf("You can attack twice whenever you take the Attack action."))),
    )
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 900)
@Composable
fun LevelUp_Review_AbilityScoreAndFeat_Screenshot() {
    val feat = com.github.arhor.spellbindr.domain.model.Feat(
        id = "athlete", name = "Athlete", desc = listOf("A long feat description remains readable in the review."),
        abilityBonusChoice = Choice.AbilityBonusChoice(1, listOf(mapOf(AbilityIds.DEX to 1))),
    )
    ReviewScreenshot(
        classes = listOf(simpleClass("fighter", "Fighter")),
        before = snapshot(3, mapOf("fighter" to 3), "Fighter 3"),
        after = snapshot(4, mapOf("fighter" to 4), "Fighter 4", abilityScores = AbilityScores(dexterity = 15)),
        feats = listOf(feat),
        selections = LevelUpSelections(abilityScoreDecision = AbilityScoreDecision.Feat(feat.id)),
        requirements = listOf(LevelUpRequirement.AbilityScoreImprovement(
            id = "fighter:4:asi", classId = "fighter", abilityPoints = 2, maximumAbilityScore = 20,
            allowsFeat = true, eligibleFeatIds = listOf(feat.id), selectedDecision = AbilityScoreDecision.Feat(feat.id),
        )),
    )
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 1100)
@Composable
fun LevelUp_Review_SpellcastingSpellsAndSlots_Screenshot() {
    val wizard = simpleClass("wizard", "Wizard", spellcasting = true)
    ReviewScreenshot(
        classes = listOf(wizard),
        spells = listOf(spell("light", "Light", 0), spell("shield", "Shield", 1)),
        before = snapshot(4, mapOf("wizard" to 4), "Wizard 4", sharedSpellSlots = mapOf(1 to 4, 2 to 3)),
        after = snapshot(5, mapOf("wizard" to 5), "Wizard 5", sharedSpellSlots = mapOf(1 to 4, 2 to 3, 3 to 2)),
        selections = LevelUpSelections(spellChanges = SpellChanges(learned = setOf(ClassSpellRef("wizard", "light")), addedToSpellbook = setOf(ClassSpellRef("wizard", "shield")))),
    )
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 900)
@Composable
fun LevelUp_Review_Multiclass_Screenshot() {
    ReviewScreenshot(
        classes = listOf(simpleClass("fighter", "Fighter"), simpleClass("wizard", "Wizard", spellcasting = true)),
        before = snapshot(4, mapOf("fighter" to 3, "wizard" to 1), "Fighter 3 / Wizard 1"),
        after = snapshot(5, mapOf("fighter" to 3, "wizard" to 2), "Fighter 3 / Wizard 2", sharedSpellSlots = mapOf(1 to 3, 2 to 2)),
        selectedClassId = "wizard",
    )
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 1200)
@Composable
fun LevelUp_Review_AcceptedWarning_LongContent_Screenshot() {
    val issue = LevelUpValidationIssue(LevelUpValidationCode.MulticlassPrerequisite, "The selected class does not meet the normal multiclass prerequisite; this exception was reviewed and accepted.", LevelUpValidationSeverity.Overrideable)
    ReviewScreenshot(
        classes = listOf(simpleClass("warlock", "Warlock")),
        before = snapshot(1, mapOf("warlock" to 1), "Warlock 1", pactMagic = LevelUpPactMagicCapacity(1, 1)),
        after = snapshot(2, mapOf("warlock" to 2), "Warlock 2", pactMagic = LevelUpPactMagicCapacity(1, 2)),
        selectedClassId = "warlock",
        validations = listOf(issue),
        selections = LevelUpSelections(acknowledgedIssueCodes = setOf(issue.acknowledgementId)),
        features = listOf(Feature("long-feature", "A feature with a deliberately long title that must wrap instead of clipping", List(8) { "A long description paragraph for screenshot coverage." })),
    )
}

@Composable
private fun ReviewScreenshot(
    classes: List<CharacterClass>,
    before: LevelUpSnapshot,
    after: LevelUpSnapshot,
    selectedClassId: String? = classes.firstOrNull()?.id,
    feats: List<com.github.arhor.spellbindr.domain.model.Feat> = emptyList(),
    spells: List<Spell> = emptyList(),
    features: List<Feature> = emptyList(),
    selections: LevelUpSelections = LevelUpSelections(),
    requirements: List<LevelUpRequirement> = emptyList(),
    validations: List<LevelUpValidationIssue> = emptyList(),
) {
    val state = CharacterLevelUpUiState.Content(
        characterName = "Mira Ashfall",
        plan = LevelUpPlan(before.totalLevel, "srd-5e-2014-v1", "srd-5e-2014-data-v1", selectedClassId, selections),
        preview = LevelUpPreview(before, after, requirements, validations),
        classes = classes, feats = feats, spells = spells, features = features,
        steps = listOf(CharacterLevelUpStep.Review), step = CharacterLevelUpStep.Review, currentStepIndex = 0,
    )
    ScreenshotHarness { CharacterLevelUpScreen(state, {}, Modifier) }
}

private fun snapshot(level: Int, classLevels: Map<String, Int>, name: String, abilityScores: AbilityScores = AbilityScores(), proficiencyIds: Set<String> = emptySet(), featureIds: Set<String> = emptySet(), sharedSpellSlots: Map<Int, Int> = emptyMap(), pactMagic: LevelUpPactMagicCapacity? = null) = LevelUpSnapshot(level, classLevels, name, if (level >= 5) 3 else 2, abilityScores, 30, emptyList(), proficiencyIds, emptySet(), featureIds, 0, sharedSpellSlots, pactMagic)

private fun simpleClass(id: String, name: String, spellcasting: Boolean = false) = CharacterClass(id, name, hitDie = 10, proficiencies = emptyList(), proficiencyChoices = emptyList(), savingThrows = emptyList(), spellcasting = if (spellcasting) com.github.arhor.spellbindr.domain.model.Spellcasting(emptyList(), 1, EntityRef(AbilityIds.INT)) else null, subclasses = emptyList(), levels = emptyList())

private fun spell(id: String, name: String, level: Int) = Spell(id, name, emptyList(), level, "60 feet", false, EntityRef("evocation"), "Instantaneous", "1 action", emptyList(), emptyList(), false, source = "test")
