package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep

internal fun computeGuidedSetupSteps(
    selectedClass: CharacterClass?,
    featuresById: Map<String, Feature>,
): List<GuidedStep> {
    val steps = mutableListOf(
        GuidedStep.BASICS,
        GuidedStep.CLASS,
    )

    val classChoicesNeeded = selectedClass?.let { clazz ->
        val requiresSubclass = clazz.requiresLevelOneSubclassAtLevelOne()
        val level1FeatureChoiceCount = clazz.levels
            .firstOrNull { it.level == 1 }
            ?.features
            .orEmpty()
            .count { featuresById[it]?.choice != null }
        requiresSubclass || level1FeatureChoiceCount > 0
    } == true

    if (classChoicesNeeded) steps += GuidedStep.CLASS_CHOICES

    steps += listOf(
        GuidedStep.RACE,
        GuidedStep.BACKGROUND,
        GuidedStep.ABILITY_METHOD,
        GuidedStep.ABILITY_ASSIGN,
        GuidedStep.SKILLS_PROFICIENCIES,
        GuidedStep.EQUIPMENT,
    )

    val spellsStepNeeded = selectedClass?.spellcasting?.level == 1
    if (spellsStepNeeded) steps += GuidedStep.SPELLS

    steps += GuidedStep.REVIEW

    return steps
}

internal fun CharacterClass.requiresLevelOneSubclassAtLevelOne(): Boolean {
    return id in setOf("cleric", "sorcerer", "warlock")
}
