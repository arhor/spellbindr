package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupUiState
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationIssue
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationResult
import com.github.arhor.spellbindr.utils.calculatePointBuyCost

internal data class GuidedSpellRequirementSummary(
    val cantrips: Int,
    val level1Spells: Int,
    val level1Label: String,
)

internal fun guidedIsStandardArrayValid(assignments: Map<AbilityId, Int?>): Boolean {
    val values = assignments.values.filterNotNull()
    return values.size == AbilityIds.standardOrder.size && values.sorted() == StandardArray.sorted()
}

internal fun guidedPointBuyTotalCost(scores: Map<AbilityId, Int>): Int =
    calculatePointBuyCost(scores)

internal fun isGuidedRaceSelectionComplete(
    selection: GuidedSelection,
    races: List<Race>,
): Boolean {
    val race = races.firstOrNull { it.id == selection.raceId } ?: return false
    return race.subraces.isEmpty() || race.subraces.any { it.id == selection.subraceId }
}

internal fun isGuidedBackgroundSelectionComplete(
    selection: GuidedSelection,
    backgrounds: List<Background>,
): Boolean = backgrounds.any { it.id == selection.backgroundId }

internal fun isRequirementComplete(
    requirement: GuidedChoiceRequirement,
    selections: Map<String, Set<String>>,
): Boolean {
    val selected = selections[requirement.key].orEmpty()
    if (selected.size != requirement.choice.choose) return false
    if (selected.any { it in requirement.disabledOptions }) return false

    val legalOptionIds = requirement.options.mapTo(hashSetOf()) { it.id }
    return legalOptionIds.isEmpty() || selected.all { it in legalOptionIds }
}

internal fun firstIncompleteRequirement(
    requirements: List<GuidedChoiceRequirement>,
    category: GuidedChoiceCategory,
    selections: Map<String, Set<String>>,
): GuidedChoiceRequirement? = requirements.firstOrNull {
    it.category == category && !isRequirementComplete(it, selections)
}

internal fun guidedStepForChoiceCategory(category: GuidedChoiceCategory): GuidedStep = when (category) {
    GuidedChoiceCategory.ANCESTRY -> GuidedStep.ANCESTRY_CHOICES
    GuidedChoiceCategory.PROFICIENCY,
    GuidedChoiceCategory.LANGUAGE,
    -> GuidedStep.PROFICIENCIES_LANGUAGES

    GuidedChoiceCategory.EQUIPMENT -> GuidedStep.EQUIPMENT
}

internal fun requirementBlockingMessage(
    requirement: GuidedChoiceRequirement,
    selections: Map<String, Set<String>>,
): String {
    val selectedCount = selections[requirement.key].orEmpty().count { it !in requirement.disabledOptions }
    val remaining = (requirement.choice.choose - selectedCount).coerceAtLeast(0)
    val selectionLabel = when (requirement.category) {
        GuidedChoiceCategory.PROFICIENCY -> "proficiency"
        GuidedChoiceCategory.LANGUAGE -> "language"
        GuidedChoiceCategory.ANCESTRY -> "ancestry option"
        GuidedChoiceCategory.EQUIPMENT -> "equipment option"
    }
    if (remaining == 0) {
        return "Replace the invalid $selectionLabel selection for ${requirement.sourceLabel}."
    }
    return "Select $remaining more $selectionLabel${if (remaining == 1) "" else "s"} for " +
        "${requirement.sourceLabel}."
}

/**
 * Reconciles downstream choices after class, race, subrace, or background changes.
 *
 * Requirements are processed in their canonical order. Fixed grants win over selectable duplicates, and an earlier
 * requirement wins when two requirements currently select the same proficiency or language.
 */
internal fun reconcileGuidedChoiceSelections(
    choiceSelections: Map<String, Set<String>>,
    choiceRequirements: GuidedChoiceRequirements,
    additionalActiveKeys: Set<String> = emptySet(),
): Map<String, Set<String>> {
    val fixedOptionIdsByCategory = choiceRequirements.fixedGrants
        .groupBy(GuidedFixedGrant::category)
        .mapValues { (_, grants) -> grants.mapTo(hashSetOf(), GuidedFixedGrant::optionId) }
    val selectedByCategory = mutableMapOf<GuidedChoiceCategory, MutableSet<String>>()
    val reconciled = linkedMapOf<String, Set<String>>()

    choiceRequirements.requirements.forEach { requirement ->
        val selected = choiceSelections[requirement.key].orEmpty()
        val legalOptionIds = requirement.options.mapTo(linkedSetOf()) { it.id }
        val categoryRejectsDuplicates =
            requirement.category == GuidedChoiceCategory.PROFICIENCY ||
                requirement.category == GuidedChoiceCategory.LANGUAGE
        val fixedOptionIds = fixedOptionIdsByCategory[requirement.category].orEmpty()
        val earlierSelected = selectedByCategory.getOrPut(requirement.category, ::linkedSetOf)

        val kept = selected
            .asSequence()
            .filter { legalOptionIds.isEmpty() || it in legalOptionIds }
            .filterNot { categoryRejectsDuplicates && it in fixedOptionIds }
            .filterNot { categoryRejectsDuplicates && it in earlierSelected }
            .sortedBy { optionId ->
                legalOptionIds.indexOf(optionId).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
            .take(requirement.choice.choose)
            .toCollection(linkedSetOf())

        if (kept.isNotEmpty()) {
            reconciled[requirement.key] = kept
            if (categoryRejectsDuplicates) earlierSelected += kept
        }
    }

    additionalActiveKeys.forEach { key ->
        choiceSelections[key]?.let { selected -> reconciled[key] = selected }
    }

    return reconciled
}

internal fun validateGuidedSetupContent(
    content: GuidedCharacterSetupUiState.Content,
    pointBuyBudget: Int,
): GuidedValidationResult {
    val issues = mutableListOf<GuidedValidationIssue>()

    if (content.selection.classId == null) issues += validationError("Choose a class.")
    val race = content.selection.raceId?.let { id -> content.races.firstOrNull { it.id == id } }
    if (race == null) {
        issues += validationError("Choose a race.")
    } else if (!isGuidedRaceSelectionComplete(content.selection, content.races)) {
        issues += validationError("Choose a valid subrace for ${race.name}.")
    }
    if (!isGuidedBackgroundSelectionComplete(content.selection, content.backgrounds)) {
        issues += validationError("Choose a background.")
    }

    when (content.selection.abilityMethod) {
        null -> issues += validationError("Choose an ability score method.")
        AbilityScoreMethod.STANDARD_ARRAY -> if (!guidedIsStandardArrayValid(content.selection.standardArrayAssignments)) {
            issues += validationError("Assign all ability scores using the standard array (15, 14, 13, 12, 10, 8).")
        }

        AbilityScoreMethod.POINT_BUY -> if (guidedPointBuyTotalCost(content.selection.pointBuyScores) > pointBuyBudget) {
            issues += validationError("Point buy exceeds 27 points.")
        }
    }

    content.choiceRequirements.forEach { requirement ->
        if (!isRequirementComplete(requirement, content.selection.choiceSelections)) {
            issues += validationError(
                requirementBlockingMessage(requirement, content.selection.choiceSelections),
            )
        }
    }

    val clazz = content.selection.classId?.let { id -> content.classes.firstOrNull { it.id == id } }
    if (clazz != null) {
        if (clazz.requiresLevelOneSubclassAtLevelOne() && content.selection.subclassId == null) {
            issues += validationError("Choose a subclass.")
        }

        findGuidedLevelOneFeatureChoices(
            clazz = clazz,
            subclassId = content.selection.subclassId,
            featuresById = content.featuresById,
        ).forEach { (featureId, choice) ->
            val selected =
                content.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
            if (selected.size != choice.choose) {
                issues += validationError(
                    "Select ${choice.choose} option(s) for ${content.featuresById[featureId]?.name ?: featureId}.",
                )
            }
        }

        computeGuidedSpellRequirementSummary(clazz, content.preview)?.let { req ->
            val selectedCantrips =
                content.selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
            if (req.cantrips > 0 && selectedCantrips.size != req.cantrips) {
                issues += validationError("Select ${req.cantrips} cantrip(s).")
            }
            val selectedSpells =
                content.selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
            if (req.level1Spells > 0 && selectedSpells.size != req.level1Spells) {
                issues += validationError("Select ${req.level1Spells} ${req.level1Label}.")
            }
        }
    }

    if (content.name.isBlank()) {
        issues += validationWarning("Name is empty (you can set it later).")
    }

    return GuidedValidationResult(issues = issues)
}

private fun validationError(message: String): GuidedValidationIssue =
    GuidedValidationIssue(GuidedValidationIssue.Severity.ERROR, message)

private fun validationWarning(message: String): GuidedValidationIssue =
    GuidedValidationIssue(GuidedValidationIssue.Severity.WARNING, message)
