package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spellcasting
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GuidedSetupStepPlannerTest {

    @Test
    fun `computeGuidedSetupSteps should omit conditional steps when choices are empty`() {
        // Given
        val choiceRequirements = GuidedChoiceRequirements(emptyList(), emptyList())

        // When
        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = choiceRequirements,
        )

        // Then
        assertThat(steps).containsExactly(
            GuidedStep.BASICS,
            GuidedStep.CLASS,
            GuidedStep.RACE,
            GuidedStep.BACKGROUND,
            GuidedStep.ABILITY_METHOD,
            GuidedStep.ABILITY_ASSIGN,
            GuidedStep.EQUIPMENT,
            GuidedStep.REVIEW,
        ).inOrder()
    }

    @Test
    fun `computeGuidedSetupSteps should order ancestry before proficiencies when both choices exist`() {
        // Given
        val ancestry = requirement(
            key = "race/trait/ancestry",
            category = GuidedChoiceCategory.ANCESTRY,
        )
        val language = requirement(
            key = "background/language",
            category = GuidedChoiceCategory.LANGUAGE,
        )

        // When
        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(
                requirements = listOf(ancestry, language),
                fixedGrants = emptyList(),
            ),
        )

        // Then
        assertThat(steps).containsExactly(
            GuidedStep.BASICS,
            GuidedStep.CLASS,
            GuidedStep.RACE,
            GuidedStep.BACKGROUND,
            GuidedStep.ABILITY_METHOD,
            GuidedStep.ABILITY_ASSIGN,
            GuidedStep.ANCESTRY_CHOICES,
            GuidedStep.PROFICIENCIES_LANGUAGES,
            GuidedStep.EQUIPMENT,
            GuidedStep.REVIEW,
        ).inOrder()
    }

    @Test
    fun `computeGuidedSetupSteps should retain proficiencies step when only fixed grant exists`() {
        // Given
        val grant = GuidedFixedGrant(
            optionId = "skill-perception",
            displayName = "Perception",
            category = GuidedChoiceCategory.PROFICIENCY,
            source = GuidedChoiceSource.CLASS,
            sourceId = "fighter",
            sourceLabel = "Class: Fighter",
        )

        // When
        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(emptyList(), listOf(grant)),
        )

        // Then
        assertThat(steps).contains(GuidedStep.PROFICIENCIES_LANGUAGES)
    }

    @Test
    fun `computeGuidedSetupSteps should include class and spell steps when class is level one spellcaster`() {
        // Given
        val cleric = CharacterClass(
            id = "cleric",
            name = "Cleric",
            hitDie = 8,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = Spellcasting(
                info = emptyList(),
                level = 1,
                spellcastingAbility = EntityRef("wis"),
            ),
            subclasses = emptyList(),
            levels = emptyList(),
        )

        // When
        val steps = computeGuidedSetupSteps(
            selectedClass = cleric,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(emptyList(), emptyList()),
        )

        // Then
        assertThat(steps).contains(GuidedStep.CLASS_CHOICES)
        assertThat(steps).contains(GuidedStep.SPELLS)
        assertThat(steps.indexOf(GuidedStep.CLASS_CHOICES)).isLessThan(steps.indexOf(GuidedStep.RACE))
        assertThat(steps.indexOf(GuidedStep.SPELLS)).isLessThan(steps.indexOf(GuidedStep.REVIEW))
    }

    @Test
    fun `resolveGuidedSetupStep should return closest preceding step when destination was removed`() {
        // Given
        val availableSteps = listOf(
            GuidedStep.BASICS,
            GuidedStep.CLASS,
            GuidedStep.RACE,
            GuidedStep.BACKGROUND,
            GuidedStep.ABILITY_METHOD,
            GuidedStep.ABILITY_ASSIGN,
            GuidedStep.EQUIPMENT,
            GuidedStep.REVIEW,
        )

        // When
        val ancestryDestination = resolveGuidedSetupStep(GuidedStep.ANCESTRY_CHOICES, availableSteps)
        val proficienciesDestination = resolveGuidedSetupStep(GuidedStep.PROFICIENCIES_LANGUAGES, availableSteps)

        // Then
        assertThat(ancestryDestination).isEqualTo(GuidedStep.ABILITY_ASSIGN)
        assertThat(proficienciesDestination).isEqualTo(GuidedStep.ABILITY_ASSIGN)
    }

    private fun requirement(
        key: String,
        category: GuidedChoiceCategory,
    ) = GuidedChoiceRequirement(
        key = key,
        source = GuidedChoiceSource.RACE_TRAIT,
        sourceId = "source",
        sourceLabel = "Race trait: Source",
        sourceDescription = null,
        category = category,
        choice = Choice.OptionsArrayChoice(choose = 1, from = listOf("option")),
        options = listOf(GuidedChoiceOption("option", "Option")),
        selectedOptionIds = emptySet(),
        disabledOptions = emptyMap(),
    )
}
