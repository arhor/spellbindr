package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.feature.character.guided.components.race.RaceCarousel

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun RaceCarousel_SelectedSubrace_Screenshot() {
    ScreenshotHarness {
        RaceCarousel(
            races = previewRaces,
            traitsById = previewTraits,
            selectedRaceId = "elf",
            selectedSubraceId = "high-elf",
            onRaceSelected = {},
            onSubraceSelected = { _, _ -> },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640, fontScale = 1.3f)
@Composable
fun RaceCarousel_NoSelectionLargeFont_Screenshot() {
    ScreenshotHarness {
        RaceCarousel(
            races = previewRaces,
            traitsById = previewTraits,
            selectedRaceId = null,
            selectedSubraceId = null,
            onRaceSelected = {},
            onSubraceSelected = { _, _ -> },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun RaceCarousel_HumanArtworkProof_Screenshot() {
    ScreenshotHarness {
        RaceCarousel(
            races = previewRaces,
            traitsById = previewTraits,
            selectedRaceId = "human",
            selectedSubraceId = null,
            onRaceSelected = {},
            onSubraceSelected = { _, _ -> },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private val previewRaces = listOf(
    Race(
        id = "elf",
        name = "Elf",
        traits = listOf(
            EntityRef("elf-ability"),
            EntityRef("elf-size"),
            EntityRef("elf-speed"),
            EntityRef("darkvision"),
            EntityRef("fey-ancestry"),
            EntityRef("keen-senses"),
        ),
        subraces = listOf(
            Race.Subrace(
                id = "high-elf",
                name = "High Elf",
                desc = "High elves have keen minds and a natural command of magic.",
                traits = listOf(EntityRef("high-elf-ability"), EntityRef("high-elf-cantrip")),
            ),
        ),
    ),
    Race(
        id = "dwarf",
        name = "Dwarf",
        traits = listOf(EntityRef("dwarf-size"), EntityRef("dwarf-speed"), EntityRef("stonecunning")),
        subraces = emptyList(),
    ),
    Race(
        id = "human",
        name = "Human",
        traits = listOf(EntityRef("human-size"), EntityRef("human-speed"), EntityRef("human-versatility")),
        subraces = emptyList(),
    ),
    Race(
        id = "halfling",
        name = "Halfling",
        traits = emptyList(),
        subraces = emptyList(),
    ),
)

private val previewTraits = listOf(
    Trait(
        id = "elf-ability",
        name = "Ability Score Increase",
        desc = listOf("Your Dexterity score increases by 2."),
        effects = listOf(Effect.ModifyAbilityEffect(mapOf("dex" to 2))),
    ),
    Trait(
        id = "high-elf-ability",
        name = "Ability Score Increase",
        desc = listOf("Your Intelligence score increases by 1."),
        effects = listOf(Effect.ModifyAbilityEffect(mapOf("int" to 1))),
    ),
    Trait(
        id = "elf-size",
        name = "Size",
        desc = listOf("Your size is Medium."),
        effects = listOf(Effect.ModifySizeEffect("medium")),
    ),
    Trait(
        id = "elf-speed",
        name = "Speed",
        desc = listOf("Your base walking speed is 30 feet."),
        effects = listOf(Effect.ModifySpeedEffect(30)),
    ),
    Trait(
        id = "darkvision",
        name = "Darkvision",
        desc = listOf("You see in dim light as if it were bright light."),
    ),
    Trait(
        id = "fey-ancestry",
        name = "Fey Ancestry",
        desc = listOf("You have advantage against being charmed."),
    ),
    Trait(
        id = "keen-senses",
        name = "Keen Senses",
        desc = listOf("You have proficiency in Perception."),
    ),
    Trait(
        id = "high-elf-cantrip",
        name = "Cantrip",
        desc = listOf("Choose one wizard cantrip."),
        spellChoice = Choice.OptionsArrayChoice(choose = 1, from = listOf("light", "mage-hand")),
    ),
    Trait(
        id = "human-size",
        name = "Size",
        desc = listOf("Your size is Medium."),
        effects = listOf(Effect.ModifySizeEffect("medium")),
    ),
    Trait(
        id = "human-speed",
        name = "Speed",
        desc = listOf("Your base walking speed is 30 feet."),
        effects = listOf(Effect.ModifySpeedEffect(30)),
    ),
    Trait(
        id = "human-versatility",
        name = "Versatility",
        desc = listOf("Humans are adaptable, ambitious, and found across the world."),
        effects = listOf(
            Effect.ModifyAbilityEffect(
                mapOf("str" to 1, "dex" to 1, "con" to 1, "int" to 1, "wis" to 1, "cha" to 1),
            ),
        ),
    ),
    Trait(
        id = "dwarf-size",
        name = "Size",
        desc = listOf("Your size is Medium."),
        effects = listOf(Effect.ModifySizeEffect("medium")),
    ),
    Trait(
        id = "dwarf-speed",
        name = "Speed",
        desc = listOf("Your base walking speed is 25 feet."),
        effects = listOf(Effect.ModifySpeedEffect(25)),
    ),
    Trait(
        id = "stonecunning",
        name = "Stonecunning",
        desc = listOf("You have exceptional knowledge of stonework."),
    ),
).associateBy { it.id }
