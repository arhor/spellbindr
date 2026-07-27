package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceOption
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirement
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceSource
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedFixedGrant
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuidedChoiceStepsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun proficiencies_step_shows_merged_sources_conflict_and_independent_progress() {
        val grants = listOf(
            fixedGrant(GuidedChoiceSource.CLASS, "Class: Ranger"),
            fixedGrant(GuidedChoiceSource.RACE_TRAIT, "Race trait: Keen Senses"),
        )
        val requirements = listOf(
            proficiencyRequirement(
                key = "class/proficiency/0",
                source = GuidedChoiceSource.CLASS,
                label = "Class: Ranger",
                selected = setOf("skill-survival"),
                disabled = mapOf("skill-perception" to "Race trait: Keen Senses"),
            ),
            proficiencyRequirement(
                key = "race/trait/training/proficiency",
                source = GuidedChoiceSource.RACE_TRAIT,
                label = "Race trait: Training",
                selected = emptySet(),
            ),
        )

        composeTestRule.setContent {
            AppTheme {
                ProficienciesLanguagesStep(
                    fixedGrants = grants,
                    requirements = requirements,
                    onChoiceToggled = { _, _, _ -> },
                    listState = rememberLazyListState(),
                )
            }
        }

        composeTestRule.onNodeWithText("Granted by Class: Ranger, Race trait: Keen Senses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Class").assertIsDisplayed()
        composeTestRule.onNodeWithText("Race & subrace").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Already have - Race trait: Keen Senses")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("0 of 2 choices complete")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun ancestry_step_shows_trait_context_and_completed_state() {
        val requirement = GuidedChoiceRequirement(
            key = "race/trait/draconic-ancestry/choice",
            source = GuidedChoiceSource.RACE_TRAIT,
            sourceId = "draconic-ancestry",
            sourceLabel = "Race trait: Draconic Ancestry",
            sourceDescription = "Choose the type of dragon in your ancestry.",
            category = GuidedChoiceCategory.ANCESTRY,
            choice = Choice.OptionsArrayChoice(
                choose = 1,
                from = listOf("black", "blue"),
            ),
            options = listOf(
                GuidedChoiceOption("black", "Black dragon"),
                GuidedChoiceOption("blue", "Blue dragon"),
            ),
            selectedOptionIds = setOf("blue"),
            disabledOptions = emptyMap(),
        )

        composeTestRule.setContent {
            AppTheme {
                AncestryChoicesStep(
                    requirements = listOf(requirement),
                    onChoiceToggled = { _, _, _ -> },
                    listState = rememberLazyListState(),
                )
            }
        }

        composeTestRule.onNodeWithText("✓ Race trait: Draconic Ancestry").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Choose the type of dragon in your ancestry. Choose 1. Complete.",
        ).assertIsDisplayed()
        composeTestRule
            .onNodeWithText("All 1 ancestry choices complete")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun fixedGrant(
        source: GuidedChoiceSource,
        sourceLabel: String,
    ) = GuidedFixedGrant(
        optionId = "skill-perception",
        displayName = "Perception",
        category = GuidedChoiceCategory.PROFICIENCY,
        source = source,
        sourceId = sourceLabel,
        sourceLabel = sourceLabel,
    )

    private fun proficiencyRequirement(
        key: String,
        source: GuidedChoiceSource,
        label: String,
        selected: Set<String>,
        disabled: Map<String, String> = emptyMap(),
    ) = GuidedChoiceRequirement(
        key = key,
        source = source,
        sourceId = label,
        sourceLabel = label,
        sourceDescription = null,
        category = GuidedChoiceCategory.PROFICIENCY,
        choice = Choice.ProficiencyChoice(
            choose = 2,
            from = listOf("skill-athletics", "skill-survival", "skill-perception"),
        ),
        options = listOf(
            GuidedChoiceOption("skill-athletics", "Athletics"),
            GuidedChoiceOption("skill-survival", "Survival"),
            GuidedChoiceOption("skill-perception", "Perception"),
        ),
        selectedOptionIds = selected,
        disabledOptions = disabled,
    )
}
