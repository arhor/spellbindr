package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.ui.feature.character.guided.AncestryChoicesStep
import com.github.arhor.spellbindr.ui.feature.character.guided.ProficienciesLanguagesStep
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceOption
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirement
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceSource
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedFixedGrant

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun ProficienciesLanguages_MixedConflict_Screenshot() {
    ScreenshotHarness {
        ProficienciesLanguagesStep(
            fixedGrants = listOf(
                fixedGrant(
                    id = "skill-perception",
                    name = "Perception",
                    category = GuidedChoiceCategory.PROFICIENCY,
                    source = GuidedChoiceSource.RACE_TRAIT,
                    sourceLabel = "Race trait: Keen Senses",
                ),
                fixedGrant(
                    id = "skill-perception",
                    name = "Perception",
                    category = GuidedChoiceCategory.PROFICIENCY,
                    source = GuidedChoiceSource.BACKGROUND,
                    sourceLabel = "Background: Sailor",
                ),
                fixedGrant(
                    id = "common",
                    name = "Common",
                    category = GuidedChoiceCategory.LANGUAGE,
                    source = GuidedChoiceSource.CLASS,
                    sourceLabel = "Class: Fighter",
                ),
            ),
            requirements = listOf(
                proficiencyRequirement(
                    key = "class/proficiency/0",
                    source = GuidedChoiceSource.CLASS,
                    sourceLabel = "Class: Fighter",
                    selected = setOf("skill-athletics"),
                    disabled = mapOf("skill-perception" to "Race trait: Keen Senses"),
                ),
                languageRequirement(),
            ),
            onChoiceToggled = { _, _, _ -> },
            listState = rememberLazyListState(),
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun ProficienciesLanguages_AllFixed_Screenshot() {
    ScreenshotHarness {
        ProficienciesLanguagesStep(
            fixedGrants = listOf(
                fixedGrant(
                    id = "skill-stealth",
                    name = "Stealth",
                    category = GuidedChoiceCategory.PROFICIENCY,
                    source = GuidedChoiceSource.BACKGROUND,
                    sourceLabel = "Background: Urchin",
                ),
                fixedGrant(
                    id = "thieves-tools",
                    name = "Thieves' tools",
                    category = GuidedChoiceCategory.PROFICIENCY,
                    source = GuidedChoiceSource.CLASS,
                    sourceLabel = "Class: Rogue",
                ),
                fixedGrant(
                    id = "common",
                    name = "Common",
                    category = GuidedChoiceCategory.LANGUAGE,
                    source = GuidedChoiceSource.CLASS,
                    sourceLabel = "Class: Rogue",
                ),
            ),
            requirements = emptyList(),
            onChoiceToggled = { _, _, _ -> },
            listState = rememberLazyListState(),
        )
    }
}

@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun AncestryChoices_RacialCantrip_Screenshot() {
    ScreenshotHarness {
        AncestryChoicesStep(
            requirements = listOf(
                GuidedChoiceRequirement(
                    key = "race/trait/high-elf-cantrip/spell",
                    source = GuidedChoiceSource.SUBRACE_TRAIT,
                    sourceId = "high-elf-cantrip",
                    sourceLabel = "Subrace trait: Cantrip",
                    sourceDescription = "You know one cantrip of your choice from the wizard spell list.",
                    category = GuidedChoiceCategory.ANCESTRY,
                    choice = Choice.ResourceListChoice(
                        choose = 1,
                        from = "spells",
                        where = mapOf("classes" to "wizard", "level" to "0"),
                    ),
                    options = listOf(
                        GuidedChoiceOption("acid-splash", "Acid Splash"),
                        GuidedChoiceOption("fire-bolt", "Fire Bolt"),
                        GuidedChoiceOption("light", "Light"),
                    ),
                    selectedOptionIds = setOf("fire-bolt"),
                    disabledOptions = emptyMap(),
                ),
            ),
            onChoiceToggled = { _, _, _ -> },
            listState = rememberLazyListState(),
        )
    }
}

private fun proficiencyRequirement(
    key: String,
    source: GuidedChoiceSource,
    sourceLabel: String,
    selected: Set<String>,
    disabled: Map<String, String>,
) = GuidedChoiceRequirement(
    key = key,
    source = source,
    sourceId = "fighter",
    sourceLabel = sourceLabel,
    sourceDescription = null,
    category = GuidedChoiceCategory.PROFICIENCY,
    choice = Choice.ProficiencyChoice(
        choose = 2,
        from = listOf("skill-athletics", "skill-arcana", "skill-perception"),
    ),
    options = listOf(
        GuidedChoiceOption("skill-athletics", "Athletics"),
        GuidedChoiceOption("skill-arcana", "Arcana"),
        GuidedChoiceOption("skill-perception", "Perception"),
    ),
    selectedOptionIds = selected,
    disabledOptions = disabled,
)

private fun languageRequirement() = GuidedChoiceRequirement(
    key = "background/language",
    source = GuidedChoiceSource.BACKGROUND,
    sourceId = "sage",
    sourceLabel = "Background: Sage",
    sourceDescription = null,
    category = GuidedChoiceCategory.LANGUAGE,
    choice = Choice.FromAllChoice(choose = 1),
    options = listOf(
        GuidedChoiceOption("dwarvish", "Dwarvish"),
        GuidedChoiceOption("elvish", "Elvish"),
    ),
    selectedOptionIds = emptySet(),
    disabledOptions = emptyMap(),
)

private fun fixedGrant(
    id: String,
    name: String,
    category: GuidedChoiceCategory,
    source: GuidedChoiceSource,
    sourceLabel: String,
) = GuidedFixedGrant(
    optionId = id,
    displayName = name,
    category = category,
    source = source,
    sourceId = sourceLabel,
    sourceLabel = sourceLabel,
)
