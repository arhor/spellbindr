package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupScreen
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupUiState
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedCharacterPreview
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun GuidedRaceStep_RequiredSubraceDisabled_Screenshot() {
    ScreenshotHarness {
        GuidedCharacterSetupScreen(state = guidedRacePreviewState(halfling, raceId = "halfling"))
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun GuidedRaceStep_DragonbornEnabled_Screenshot() {
    ScreenshotHarness {
        GuidedCharacterSetupScreen(state = guidedRacePreviewState(dragonborn, raceId = "dragonborn"))
    }
}

@PreviewTest
@Preview(widthDp = 280, heightDp = 500)
@Composable
fun GuidedRaceStep_LongTraitsNarrow_Screenshot() {
    ScreenshotHarness {
        GuidedCharacterSetupScreen(state = guidedRacePreviewState(dragonborn, raceId = "dragonborn"))
    }
}

private fun guidedRacePreviewState(
    race: Race,
    raceId: String,
): GuidedCharacterSetupUiState.Content = GuidedCharacterSetupUiState.Content(
    step = GuidedStep.RACE,
    steps = GuidedStep.entries.toList(),
    currentStepIndex = GuidedStep.entries.indexOf(GuidedStep.RACE),
    totalSteps = GuidedStep.entries.size,
    name = "Preview hero",
    classes = emptyList(),
    races = listOf(race),
    backgrounds = emptyList(),
    languages = emptyList(),
    equipment = emptyList(),
    traitsById = previewRaceTraits.associateBy(Trait::id),
    featuresById = emptyMap(),
    languagesById = emptyMap(),
    equipmentById = emptyMap(),
    spells = emptyList(),
    spellsById = emptyMap(),
    referenceDataVersion = 1,
    selection = GuidedSelection(
        classId = null,
        subclassId = null,
        raceId = raceId,
        subraceId = null,
        backgroundId = null,
        abilityMethod = null,
        standardArrayAssignments = emptyMap(),
        pointBuyScores = emptyMap(),
        choiceSelections = emptyMap(),
    ),
    preview = GuidedCharacterPreview(
        abilityScores = AbilityScores(),
        maxHitPoints = 1,
        armorClass = 10,
        speed = 30,
        languagesCount = 0,
        proficienciesCount = 0,
    ),
    isSaving = false,
)

private val halfling = Race(
    id = "halfling",
    name = "Halfling",
    traits = listOf(EntityRef("halfling-size"), EntityRef("halfling-speed"), EntityRef("lucky")),
    subraces = listOf(
        Race.Subrace("lightfoot", "Lightfoot", "Naturally stealthy and sociable.", emptyList()),
        Race.Subrace("stout", "Stout", "Hardy and resilient.", emptyList()),
    ),
)

private val dragonborn = Race(
    id = "dragonborn",
    name = "Dragonborn",
    traits = listOf(
        EntityRef("dragonborn-ability"),
        EntityRef("dragonborn-size"),
        EntityRef("dragonborn-speed"),
        EntityRef("draconic-ancestry"),
        EntityRef("breath-weapon"),
        EntityRef("damage-resistance"),
        EntityRef("long-trait"),
    ),
    subraces = emptyList(),
)

private val previewRaceTraits = listOf(
    Trait("halfling-size", "Size", listOf("Your size is Small."), listOf(Effect.ModifySizeEffect("small"))),
    Trait("halfling-speed", "Speed", listOf("Your speed is 25 feet."), listOf(Effect.ModifySpeedEffect(25))),
    Trait("lucky", "Lucky", listOf("You can reroll a natural 1.")),
    Trait(
        "dragonborn-ability",
        "Ability Score Increase",
        listOf("Your Strength increases by 2 and Charisma by 1."),
        listOf(Effect.ModifyAbilityEffect(mapOf("str" to 2, "cha" to 1))),
    ),
    Trait("dragonborn-size", "Size", listOf("Your size is Medium."), listOf(Effect.ModifySizeEffect("medium"))),
    Trait("dragonborn-speed", "Speed", listOf("Your speed is 30 feet."), listOf(Effect.ModifySpeedEffect(30))),
    Trait(
        "draconic-ancestry",
        "Draconic Ancestry",
        listOf("Choose your draconic ancestry later."),
        draconicAncestryChoice = Choice.OptionsArrayChoice(choose = 1, from = listOf("black", "blue")),
    ),
    Trait("breath-weapon", "Breath Weapon", listOf("Exhale destructive energy.")),
    Trait("damage-resistance", "Damage Resistance", listOf("Resist your ancestry's damage type.")),
    Trait("long-trait", "Ancient Draconic Heritage", listOf("A deliberately long summary for truncation coverage.")),
)
