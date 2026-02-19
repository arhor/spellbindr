package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupUiState
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationIssue
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationResult

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
    scores.values.sumOf(::pointBuyCost)

internal fun validateGuidedSetupContent(
    content: GuidedCharacterSetupUiState.Content,
    pointBuyBudget: Int,
): GuidedValidationResult {
    val issues = mutableListOf<GuidedValidationIssue>()

    if (content.selection.classId == null) issues += validationError("Choose a class.")
    if (content.selection.raceId == null) issues += validationError("Choose a race.")
    if (content.selection.backgroundId == null) issues += validationError("Choose a background.")

    when (content.selection.abilityMethod) {
        null -> issues += validationError("Choose an ability score method.")
        AbilityScoreMethod.STANDARD_ARRAY -> if (!guidedIsStandardArrayValid(content.selection.standardArrayAssignments)) {
            issues += validationError("Assign all ability scores using the standard array (15, 14, 13, 12, 10, 8).")
        }

        AbilityScoreMethod.POINT_BUY -> if (guidedPointBuyTotalCost(content.selection.pointBuyScores) > pointBuyBudget) {
            issues += validationError("Point buy exceeds 27 points.")
        }
    }

    val background = content.selection.backgroundId?.let { id -> content.backgrounds.firstOrNull { it.id == id } }
    val bgLangChoice = background?.languageChoice
    if (bgLangChoice != null) {
        val selected =
            content.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty()
        if (selected.size != bgLangChoice.choose) {
            issues += validationError("Select ${bgLangChoice.choose} background language(s).")
        }
    }

    val bgEquipChoice = background?.equipmentChoice
    if (bgEquipChoice != null) {
        val selected =
            content.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty()
        if (selected.size != bgEquipChoice.choose) {
            issues += validationError("Select ${bgEquipChoice.choose} background equipment item(s).")
        }
    }

    val race = content.selection.raceId?.let { id -> content.races.firstOrNull { it.id == id } }
    if (race != null) {
        val traitIds = buildList {
            addAll(race.traits.map { it.id })
            val subrace = content.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull { content.traitsById[it] }.forEach { trait ->
            trait.abilityBonusChoice?.let { choice ->
                val selected = content.selection.choiceSelections[
                    GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id)
                ].orEmpty()
                if (selected.size != choice.choose) {
                    issues += validationError("Select ${choice.choose} race ability bonus option(s).")
                }
            }
            trait.languageChoice?.let { choice ->
                val selected =
                    content.selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.size != choice.choose) {
                    issues += validationError("Select ${choice.choose} race language option(s).")
                }
            }
            trait.proficiencyChoice?.let { choice ->
                val selected = content.selection.choiceSelections[
                    GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id)
                ].orEmpty()
                if (selected.size != choice.choose) {
                    issues += validationError("Select ${choice.choose} race proficiency option(s).")
                }
            }
            trait.draconicAncestryChoice?.let { choice ->
                val selected = content.selection.choiceSelections[
                    GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id)
                ].orEmpty()
                if (selected.size != choice.choose) {
                    issues += validationError("Select ${choice.choose} option(s) for ${trait.name}.")
                }
            }
            trait.spellChoice?.let { choice ->
                val selected =
                    content.selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(
                        trait.id
                    )].orEmpty()
                if (selected.size != choice.choose) {
                    issues += validationError("Select ${choice.choose} spell option(s) for ${trait.name}.")
                }
            }
        }
    }

    val clazz = content.selection.classId?.let { id -> content.classes.firstOrNull { it.id == id } }
    if (clazz != null) {
        if (clazz.requiresLevelOneSubclassAtLevelOne() && content.selection.subclassId == null) {
            issues += validationError("Choose a subclass.")
        }

        clazz.proficiencyChoices.forEachIndexed { index, choice ->
            val selected =
                content.selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(
                    index
                )]
                    .orEmpty()
            if (selected.size != choice.choose) {
                issues += validationError("Select ${choice.choose} class proficiency option(s).")
            }
        }

        findGuidedLevelOneFeatureChoices(clazz, content.featuresById).forEach { (featureId, choice) ->
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
