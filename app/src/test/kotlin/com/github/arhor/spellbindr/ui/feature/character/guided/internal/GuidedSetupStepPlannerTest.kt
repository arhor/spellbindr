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
    fun `planner omits empty conditional choice steps`() {
        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(emptyList(), emptyList()),
        )

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
    fun `planner places ancestry and proficiencies in target order`() {
        val ancestry = requirement(
            key = "race/trait/ancestry",
            category = GuidedChoiceCategory.ANCESTRY,
        )
        val language = requirement(
            key = "background/language",
            category = GuidedChoiceCategory.LANGUAGE,
        )

        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(
                requirements = listOf(ancestry, language),
                fixedGrants = emptyList(),
            ),
        )

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
    fun `fixed grant retains proficiencies step without selectable requirements`() {
        val grant = GuidedFixedGrant(
            optionId = "skill-perception",
            displayName = "Perception",
            category = GuidedChoiceCategory.PROFICIENCY,
            source = GuidedChoiceSource.CLASS,
            sourceId = "fighter",
            sourceLabel = "Class: Fighter",
        )

        val steps = computeGuidedSetupSteps(
            selectedClass = null,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(emptyList(), listOf(grant)),
        )

        assertThat(steps).contains(GuidedStep.PROFICIENCIES_LANGUAGES)
    }

    @Test
    fun `level one spellcaster with class choice gets both conditional steps`() {
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

        val steps = computeGuidedSetupSteps(
            selectedClass = cleric,
            featuresById = emptyMap(),
            choiceRequirements = GuidedChoiceRequirements(emptyList(), emptyList()),
        )

        assertThat(steps).contains(GuidedStep.CLASS_CHOICES)
        assertThat(steps).contains(GuidedStep.SPELLS)
        assertThat(steps.indexOf(GuidedStep.CLASS_CHOICES)).isLessThan(steps.indexOf(GuidedStep.RACE))
        assertThat(steps.indexOf(GuidedStep.SPELLS)).isLessThan(steps.indexOf(GuidedStep.REVIEW))
    }

    @Test
    fun `removed conditional destination resolves to closest preceding step`() {
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

        assertThat(
            resolveGuidedSetupStep(GuidedStep.ANCESTRY_CHOICES, availableSteps),
        ).isEqualTo(GuidedStep.ABILITY_ASSIGN)
        assertThat(
            resolveGuidedSetupStep(GuidedStep.PROFICIENCIES_LANGUAGES, availableSteps),
        ).isEqualTo(GuidedStep.ABILITY_ASSIGN)
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
