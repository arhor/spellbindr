package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpFeatureSpellGrantRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpSpellOption
import com.github.arhor.spellbindr.domain.model.LevelUpSpellReplacementRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpScreen
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpStep
import com.github.arhor.spellbindr.ui.feature.character.levelup.CharacterLevelUpUiState

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_Loading_Compact_Screenshot() {
    LevelUpScreenshot(CharacterLevelUpUiState.Loading)
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_Class_Compact_Screenshot() {
    val fighter = characterClass("fighter", "Fighter")
    val wizard = characterClass("wizard", "Wizard")
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Class,
        classes = listOf(fighter, wizard),
        requirements = listOf(LevelUpRequirement.ClassSelection(
            eligibleClassIds = listOf(fighter.id, wizard.id),
            selectedClassId = fighter.id,
        )),
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_SubclassChoices_Compact_Screenshot() {
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Choices,
        requirements = listOf(
            LevelUpRequirement.SubclassSelection(
                id = "fighter:3:subclass",
                classId = "fighter",
                options = listOf(
                    LevelUpChoiceOption("champion", "Champion"),
                    LevelUpChoiceOption("battle-master", "Battle Master"),
                ),
                selectedSubclassId = "champion",
            ),
            LevelUpRequirement.ChoiceSelection(
                id = "fighting-style",
                sourceId = "fighter",
                label = "Fighting Style",
                choice = Choice.OptionsArrayChoice(1, listOf("defense", "dueling")),
                selectedOptionIds = setOf("defense"),
                category = LevelUpChoiceCategory.Feature,
                options = listOf(
                    LevelUpChoiceOption("defense", "Defense"),
                    LevelUpChoiceOption("dueling", "Dueling"),
                ),
            ),
        ),
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_HitPoints_Compact_Screenshot() {
    val gain = HitPointGain.Rolled(7)
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.HitPoints,
        selections = LevelUpSelections(hitPointGain = gain),
        requirements = listOf(LevelUpRequirement.HitPoints(hitDie = 10, fixedGain = 6, selectedGain = gain)),
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_AbilityFeat_Compact_Screenshot() {
    val feat = Feat(
        id = "athlete",
        name = "Athlete",
        desc = emptyList(),
        abilityBonusChoice = Choice.AbilityBonusChoice(1, listOf(mapOf(AbilityIds.DEX to 1))),
    )
    val decision = AbilityScoreDecision.Feat(feat.id)
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.AbilityScore,
        feats = listOf(feat),
        selections = LevelUpSelections(
            abilityScoreDecision = decision,
            featChoices = mapOf("athlete:ability-bonus" to setOf(AbilityIds.DEX)),
        ),
        requirements = listOf(
            LevelUpRequirement.AbilityScoreImprovement(
                id = "fighter:4:asi",
                classId = "fighter",
                abilityPoints = 2,
                maximumAbilityScore = 20,
                allowsFeat = true,
                eligibleFeatIds = listOf(feat.id),
                selectedDecision = decision,
            ),
            LevelUpRequirement.ChoiceSelection(
                id = "athlete:ability-bonus",
                sourceId = feat.id,
                label = feat.name,
                choice = feat.abilityBonusChoice!!,
                selectedOptionIds = setOf(AbilityIds.DEX),
                category = LevelUpChoiceCategory.Feat,
                options = listOf(LevelUpChoiceOption(AbilityIds.DEX, "+1 DEX")),
            ),
        ),
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 800)
@Composable
fun LevelUp_SpellDecisions_Compact_Screenshot() {
    val changes = SpellChanges(
        learned = setOf(ClassSpellRef("wizard", "light")),
        addedToSpellbook = setOf(ClassSpellRef("wizard", "shield")),
        featureLearned = mapOf("magical-secrets" to setOf(ClassSpellRef("wizard", "guidance"))),
        replacementSourceSpellId = "magic-missile",
    )
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Spells,
        selections = LevelUpSelections(spellChanges = changes),
        requirements = listOf(LevelUpRequirement.SpellDecisions(
            id = "wizard:2:spells",
            classId = "wizard",
            classLevel = 2,
            policyId = "spellbook",
            changes = changes,
            requiredCantripCount = 1,
            cantripCandidates = listOf(LevelUpSpellOption("light", "Light", 0)),
            requiredKnownSpellCount = 1,
            knownSpellCandidates = listOf(LevelUpSpellOption("detect-magic", "Detect Magic", 1)),
            featureSpellGrants = listOf(LevelUpFeatureSpellGrantRequirement(
                featureId = "magical-secrets",
                label = "Magical Secrets",
                requiredCount = 1,
                candidates = listOf(LevelUpSpellOption("guidance", "Guidance", 0)),
                selectedSpellIds = setOf("guidance"),
            )),
            replacement = LevelUpSpellReplacementRequirement(
                sourceCandidates = listOf(LevelUpSpellOption("magic-missile", "Magic Missile", 1)),
                replacementCandidates = listOf(LevelUpSpellOption("sleep", "Sleep", 1)),
                selectedSourceSpellId = "magic-missile",
            ),
            requiredSpellbookAdditionCount = 1,
            spellbookCandidates = listOf(LevelUpSpellOption("shield", "Shield", 1)),
            preparationCapacity = 5,
        )),
    ))
}

@PreviewTest
@PreviewLightDark
@Composable
fun LevelUp_ReviewWarnings_LightDark_Screenshot() {
    val override = LevelUpValidationIssue(
        LevelUpValidationCode.MulticlassPrerequisite,
        "Your ability scores do not meet the normal multiclass prerequisite.",
        LevelUpValidationSeverity.Overrideable,
    )
    val blocking = LevelUpValidationIssue(
        LevelUpValidationCode.ChoiceRequired,
        "One required choice is unresolved.",
        LevelUpValidationSeverity.Blocking,
    )
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Review,
        validations = listOf(override, blocking),
        requirements = listOf(LevelUpRequirement.Acknowledgement(
            id = override.acknowledgementId,
            issue = override,
            acknowledged = false,
        )),
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_ReviewPersistenceRetry_Compact_Screenshot() {
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Review,
        persistenceMessage = "Unable to save. Your reviewed choices are ready to retry.",
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_ReviewStaleRetry_Compact_Screenshot() {
    LevelUpScreenshot(levelUpState(
        step = CharacterLevelUpStep.Review,
        staleMessage = "The character changed. Reload this draft before confirming.",
    ))
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun LevelUp_ReviewSaving_Compact_Screenshot() {
    LevelUpScreenshot(levelUpState(step = CharacterLevelUpStep.Review, isSaving = true))
}

@Composable
private fun LevelUpScreenshot(state: CharacterLevelUpUiState) {
    ScreenshotHarness {
        CharacterLevelUpScreen(state = state, dispatch = {}, modifier = Modifier)
    }
}

private fun levelUpState(
    step: CharacterLevelUpStep,
    classes: List<CharacterClass> = emptyList(),
    feats: List<Feat> = emptyList(),
    selections: LevelUpSelections = LevelUpSelections(),
    requirements: List<LevelUpRequirement> = emptyList(),
    validations: List<LevelUpValidationIssue> = emptyList(),
    isSaving: Boolean = false,
    staleMessage: String? = null,
    persistenceMessage: String? = null,
): CharacterLevelUpUiState.Content {
    val before = levelUpSnapshot(3)
    val steps = listOf(
        CharacterLevelUpStep.Class,
        CharacterLevelUpStep.Choices,
        CharacterLevelUpStep.HitPoints,
        CharacterLevelUpStep.AbilityScore,
        CharacterLevelUpStep.Spells,
        CharacterLevelUpStep.Review,
    )
    return CharacterLevelUpUiState.Content(
        characterName = "Mira Ashfall",
        plan = LevelUpPlan(
            expectedTotalLevel = 3,
            rulesetId = "srd-5e-2014-v1",
            referenceDataVersion = "srd-5e-2014-data-v1",
            selectedClassId = "fighter",
            selections = selections,
        ),
        preview = LevelUpPreview(before, levelUpSnapshot(4), requirements, validations),
        classes = classes,
        feats = feats,
        spells = emptyList(),
        steps = steps,
        step = step,
        currentStepIndex = steps.indexOf(step),
        isSaving = isSaving,
        staleMessage = staleMessage,
        persistenceMessage = persistenceMessage,
    )
}

private fun levelUpSnapshot(level: Int) = LevelUpSnapshot(
    totalLevel = level,
    classLevels = mapOf("fighter" to level),
    classDisplayName = "Fighter $level",
    proficiencyBonus = if (level >= 5) 3 else 2,
    abilityScores = AbilityScores(strength = 16, dexterity = 14, constitution = 14),
    maximumHitPoints = if (level == 3) 28 else 37,
    hitDicePools = emptyList(),
    proficiencyIds = emptySet(),
    savingThrowAbilityIds = emptySet(),
    featureIds = emptySet(),
    sharedCasterLevel = 0,
    sharedSpellSlots = emptyMap(),
)

private fun characterClass(id: String, name: String) = CharacterClass(
    id = id,
    name = name,
    hitDie = 10,
    proficiencies = emptyList(),
    proficiencyChoices = emptyList(),
    savingThrows = emptyList(),
    subclasses = emptyList(),
    levels = emptyList(),
)
