package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpValidationReviewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `warning only review should show passive finding and keep confirmation enabled`() {
        val warning = issue(
            code = LevelUpValidationCode.ExperienceThreshold,
            message = "Level-up timing is unusual.",
            severity = LevelUpValidationSeverity.Informational,
        )

        setContent(reviewState(validations = listOf(warning)))

        composeTestRule.onNodeWithText("Review findings").assertExists()
        composeTestRule.onNodeWithText("Warning").assertExists()
        composeTestRule.onNodeWithText(warning.message).assertExists()
        composeTestRule.onNodeWithText("No action required.").assertExists()
        composeTestRule.onNodeWithText("Confirm level up").assertIsEnabled()
        assertThat(composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun `accepted exception only review should identify affected class choice`() {
        val exception = issue(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            message = "Ability prerequisites for Fighter are not met.",
            severity = LevelUpValidationSeverity.Overrideable,
        )

        setContent(
            reviewState(
                validations = listOf(exception),
                acceptedIssueCodes = setOf(exception.acknowledgementId),
            ),
        )

        composeTestRule.onNodeWithText("Review findings").assertExists()
        composeTestRule.onNodeWithText("Accepted rule exception").assertExists()
        composeTestRule.onNodeWithText(exception.message).assertExists()
        composeTestRule.onNodeWithText("Class choice: Fighter").assertExists()
        composeTestRule.onNodeWithText("Confirm level up").assertIsEnabled()
        assertThat(composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes()).hasSize(1)
    }

    @Test
    fun `manual hit point exception must be accepted before confirmation`() {
        val exception = issue(
            code = LevelUpValidationCode.ManualHitPointGainOverride,
            message = "Manual hit point gain of 14 overrides the rules-derived fixed gain of 6 or a rolled result from 1 to 10.",
            severity = LevelUpValidationSeverity.Overrideable,
        )

        setContent(reviewState(validations = listOf(exception)))
        composeTestRule.onNodeWithText(exception.message).assertExists()
        composeTestRule.onNodeWithText("Confirm level up").assertIsNotEnabled()
    }

    @Test
    fun `mixed review should keep blocking error outside accepted exceptions`() {
        val warning = issue(
            code = LevelUpValidationCode.ExperienceThreshold,
            message = "Level-up timing is unusual.",
            severity = LevelUpValidationSeverity.Informational,
        )
        val exception = issue(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            message = "Ability prerequisites for Fighter are not met.",
            severity = LevelUpValidationSeverity.Overrideable,
        )
        val blocking = issue(
            code = LevelUpValidationCode.ChoiceRequired,
            message = "Choose a class to level up.",
            severity = LevelUpValidationSeverity.Blocking,
        )

        setContent(
            reviewState(
                validations = listOf(warning, exception, blocking),
                acceptedIssueCodes = setOf(exception.acknowledgementId),
            ),
        )

        composeTestRule.onNodeWithText(warning.message).assertExists()
        composeTestRule.onNodeWithText(exception.message).assertExists()
        composeTestRule.onNodeWithText(blocking.message).assertExists()
        assertThat(
            composeTestRule.onAllNodesWithText("Accepted rule exception").fetchSemanticsNodes(),
        ).hasSize(1)
        composeTestRule.onNodeWithText("Confirm level up").assertIsNotEnabled()
        assertThat(composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes()).hasSize(1)
    }

    @Test
    fun `review findings section should be omitted when there are no warning or exception findings`() {
        setContent(reviewState(validations = emptyList()))

        composeTestRule.onNodeWithText("Review findings").assertDoesNotExist()
    }

    private fun setContent(state: CharacterLevelUpUiState.Content) {
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpScreen(state = state, dispatch = {})
            }
        }
    }

    private fun reviewState(
        validations: List<LevelUpValidationIssue>,
        acceptedIssueCodes: Set<String> = emptySet(),
    ): CharacterLevelUpUiState.Content {
        val before = snapshot()
        val acknowledgements = validations
            .filter { it.severity == LevelUpValidationSeverity.Overrideable }
            .map { issue ->
                LevelUpRequirement.Acknowledgement(
                    id = issue.acknowledgementId,
                    issue = issue,
                    acknowledged = issue.acknowledgementId in acceptedIssueCodes,
                )
            }
        return CharacterLevelUpUiState.Content(
            characterName = "Test hero",
            plan = LevelUpPlan(
                expectedTotalLevel = before.totalLevel,
                rulesetId = "srd-5e-2014-v1",
                referenceDataVersion = "test-v1",
                selectedClassId = "fighter",
                selections = LevelUpSelections(acknowledgedIssueCodes = acceptedIssueCodes),
            ),
            preview = LevelUpPreview(
                before = before,
                after = before.copy(totalLevel = before.totalLevel + 1),
                requirements = acknowledgements,
                validations = validations,
            ),
            classes = listOf(characterClass()),
            feats = emptyList(),
            spells = emptyList(),
            steps = listOf(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review),
            step = CharacterLevelUpStep.Review,
            currentStepIndex = 1,
        )
    }

    private fun issue(
        code: LevelUpValidationCode,
        message: String,
        severity: LevelUpValidationSeverity,
    ) = LevelUpValidationIssue(code, message, severity)

    private fun snapshot() = LevelUpSnapshot(
        totalLevel = 3,
        classLevels = mapOf("fighter" to 3),
        classDisplayName = "Fighter 3",
        proficiencyBonus = 2,
        abilityScores = AbilityScores(),
        maximumHitPoints = 24,
        hitDicePools = emptyList(),
        proficiencyIds = emptySet(),
        savingThrowAbilityIds = emptySet(),
        featureIds = emptySet(),
        sharedCasterLevel = 0,
        sharedSpellSlots = emptyMap(),
    )

    private fun characterClass() = CharacterClass(
        id = "fighter",
        name = "Fighter",
        hitDie = 10,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )
}
