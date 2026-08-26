package io.github.arhor.dnd.companion.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpPreview
import io.github.arhor.dnd.companion.domain.model.LevelUpRequirement
import io.github.arhor.dnd.companion.domain.model.LevelUpSnapshot
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationCode
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationIssue
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationSeverity
import io.github.arhor.dnd.companion.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpGuidanceScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `informational guidance should render without acknowledgement and keep confirmation enabled`() {
        val guidance = issue(
            code = LevelUpValidationCode.ExperienceThreshold,
            message = "You can review the experience threshold before confirming.",
            severity = LevelUpValidationSeverity.Informational,
        )

        setContent(reviewState(validations = listOf(guidance)))

        composeTestRule.onNodeWithText(guidance.message).assertExists()
        composeTestRule.onNodeWithText("Confirm level up").assertIsEnabled()
        assertThat(composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun `informational guidance should coexist with blocking and overrideable findings`() {
        val guidance = issue(
            code = LevelUpValidationCode.ExperienceThreshold,
            message = "Informational guidance.",
            severity = LevelUpValidationSeverity.Informational,
        )
        val warning = issue(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            message = "Overrideable warning.",
            severity = LevelUpValidationSeverity.Overrideable,
        )
        val blocking = issue(
            code = LevelUpValidationCode.ChoiceRequired,
            message = "Blocking error.",
            severity = LevelUpValidationSeverity.Blocking,
        )
        val acknowledgement = LevelUpRequirement.Acknowledgement(
            id = warning.acknowledgementId,
            issue = warning,
            acknowledged = false,
        )

        setContent(
            reviewState(
                validations = listOf(guidance, warning, blocking),
                requirements = listOf(acknowledgement),
            ),
        )

        composeTestRule.onNodeWithText(guidance.message).assertExists()
        composeTestRule.onNodeWithText(warning.message).assertExists()
        composeTestRule.onNodeWithText(blocking.message).assertExists()
        assertThat(composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes()).hasSize(1)
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
        requirements: List<LevelUpRequirement> = emptyList(),
    ): CharacterLevelUpUiState.Content {
        val before = snapshot()
        return CharacterLevelUpUiState.Content(
            characterName = "Test hero",
            plan = LevelUpPlan(
                expectedTotalLevel = before.totalLevel,
                rulesetId = "srd-5e-2014-v1",
                referenceDataVersion = "test-v1",
                selectedClassId = "fighter",
            ),
            preview = LevelUpPreview(
                before = before,
                after = before.copy(totalLevel = before.totalLevel + 1),
                requirements = requirements,
                validations = validations,
            ),
            classes = emptyList(),
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
}
