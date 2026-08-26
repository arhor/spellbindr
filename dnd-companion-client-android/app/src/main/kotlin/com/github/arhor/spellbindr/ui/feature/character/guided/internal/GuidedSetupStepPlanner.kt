package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep

internal fun computeGuidedSetupSteps(
    selectedClass: CharacterClass?,
    selectedSubclassId: String? = null,
    featuresById: Map<String, Feature>,
    choiceRequirements: GuidedChoiceRequirements,
): List<GuidedStep> {
    val steps = mutableListOf(
        GuidedStep.BASICS,
        GuidedStep.CLASS,
    )

    val classChoicesNeeded = selectedClass?.let { clazz ->
        val requiresSubclass = clazz.requiresLevelOneSubclassAtLevelOne()
        val level1FeatureChoiceCount = findGuidedLevelOneFeatureChoices(
            clazz = clazz,
            subclassId = selectedSubclassId,
            featuresById = featuresById,
        ).size
        requiresSubclass || level1FeatureChoiceCount > 0
    } == true

    if (classChoicesNeeded) steps += GuidedStep.CLASS_CHOICES

    steps += listOf(
        GuidedStep.RACE,
        GuidedStep.BACKGROUND,
        GuidedStep.ABILITY_METHOD,
        GuidedStep.ABILITY_ASSIGN,
    )

    if (choiceRequirements.requirements.any { it.category == GuidedChoiceCategory.ANCESTRY }) {
        steps += GuidedStep.ANCESTRY_CHOICES
    }

    val hasProficienciesOrLanguages =
        choiceRequirements.requirements.any {
            it.category == GuidedChoiceCategory.PROFICIENCY ||
                it.category == GuidedChoiceCategory.LANGUAGE
        } ||
            choiceRequirements.fixedGrants.any {
                it.category == GuidedChoiceCategory.PROFICIENCY ||
                    it.category == GuidedChoiceCategory.LANGUAGE
            }
    if (hasProficienciesOrLanguages) {
        steps += GuidedStep.PROFICIENCIES_LANGUAGES
    }

    steps += GuidedStep.EQUIPMENT

    val spellsStepNeeded = selectedClass?.spellcasting?.level == 1
    if (spellsStepNeeded) steps += GuidedStep.SPELLS

    steps += GuidedStep.REVIEW

    return steps
}

/**
 * Resolves a destination that disappeared after an upstream selection changed.
 *
 * Conditional steps map to the closest preceding step in the complete guided order, which keeps users near the
 * choice they just changed instead of unexpectedly sending them back to Basics.
 */
internal fun resolveGuidedSetupStep(
    requestedStep: GuidedStep,
    availableSteps: List<GuidedStep>,
): GuidedStep {
    require(availableSteps.isNotEmpty()) { "Guided setup must contain at least one step." }
    if (requestedStep in availableSteps) return requestedStep

    val requestedIndex = completeGuidedStepOrder.indexOf(requestedStep)
    if (requestedIndex < 0) return availableSteps.first()

    return completeGuidedStepOrder
        .subList(0, requestedIndex)
        .asReversed()
        .firstOrNull { it in availableSteps }
        ?: availableSteps.first()
}

private val completeGuidedStepOrder = listOf(
    GuidedStep.BASICS,
    GuidedStep.CLASS,
    GuidedStep.CLASS_CHOICES,
    GuidedStep.RACE,
    GuidedStep.BACKGROUND,
    GuidedStep.ABILITY_METHOD,
    GuidedStep.ABILITY_ASSIGN,
    GuidedStep.ANCESTRY_CHOICES,
    GuidedStep.PROFICIENCIES_LANGUAGES,
    GuidedStep.EQUIPMENT,
    GuidedStep.SPELLS,
    GuidedStep.REVIEW,
)

internal fun CharacterClass.requiresLevelOneSubclassAtLevelOne(): Boolean {
    return id in setOf("cleric", "sorcerer", "warlock")
}
