package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.serialization.Serializable

@Serializable
enum class CharacterLevelUpStep(val title: String) {
    Class("Class"),
    Choices("Subclass & choices"),
    HitPoints("Hit points"),
    AbilityScore("ASI or feat"),
    Spells("Spells"),
    Review("Review"),
}

internal fun characterLevelUpSteps(requirements: List<LevelUpRequirement>): List<CharacterLevelUpStep> = buildList {
    add(CharacterLevelUpStep.Class)
    if (requirements.any {
            it is LevelUpRequirement.SubclassSelection ||
                it is LevelUpRequirement.ChoiceSelection && it.category != LevelUpChoiceCategory.Feat
        }
    ) {
        add(CharacterLevelUpStep.Choices)
    }
    if (requirements.any { it is LevelUpRequirement.HitPoints }) add(CharacterLevelUpStep.HitPoints)
    if (requirements.any { it is LevelUpRequirement.AbilityScoreImprovement }) add(CharacterLevelUpStep.AbilityScore)
    if (requirements.any { it is LevelUpRequirement.SpellDecisions }) add(CharacterLevelUpStep.Spells)
    add(CharacterLevelUpStep.Review)
}

sealed interface CharacterLevelUpUiState {
    @Immutable data object Loading : CharacterLevelUpUiState
    @Immutable data class Failure(val message: String) : CharacterLevelUpUiState
    @Immutable data class Unavailable(val title: String, val explanation: String) : CharacterLevelUpUiState

    @Immutable
    data class Content(
        val characterName: String,
        val plan: LevelUpPlan,
        val preview: LevelUpPreview,
        val classes: List<CharacterClass>,
        val feats: List<Feat>,
        val spells: List<Spell>,
        val steps: List<CharacterLevelUpStep>,
        val step: CharacterLevelUpStep,
        val currentStepIndex: Int,
        val isSaving: Boolean = false,
        val staleMessage: String? = null,
        val persistenceMessage: String? = null,
    ) : CharacterLevelUpUiState {
        val isReview: Boolean get() = step == CharacterLevelUpStep.Review
        val canConfirm: Boolean get() = isReview && preview.canConfirm && !isSaving
        val canAdvance: Boolean get() = !isSaving && canAdvanceFromCurrentStep()
        val blockingIssues: List<LevelUpValidationIssue>
            get() = preview.validations.filter { it.severity == LevelUpValidationSeverity.Blocking }
        val overrideableIssues: List<LevelUpValidationIssue>
            get() = preview.validations.filter { it.severity == LevelUpValidationSeverity.Overrideable }
        val informationalIssues: List<LevelUpValidationIssue>
            get() = preview.validations.filter { it.severity == LevelUpValidationSeverity.Informational }

        private fun canAdvanceFromCurrentStep(): Boolean = when (step) {
            CharacterLevelUpStep.Class -> preview.requirements
                .filterIsInstance<LevelUpRequirement.ClassSelection>()
                .singleOrNull()
                ?.selectedClassId
                ?.let { selected -> classes.any { it.id == selected } } == true

            CharacterLevelUpStep.Choices -> preview.requirements.all { requirement ->
                when (requirement) {
                    is LevelUpRequirement.SubclassSelection -> requirement.selectedSubclassId in
                        requirement.options.map(LevelUpChoiceOption::id)
                    is LevelUpRequirement.ChoiceSelection ->
                        requirement.category == LevelUpChoiceCategory.Feat || requirement.isComplete()
                    else -> true
                }
            }

            CharacterLevelUpStep.HitPoints -> preview.requirements
                .filterIsInstance<LevelUpRequirement.HitPoints>()
                .singleOrNull()
                ?.hasValidSelection() == true

            CharacterLevelUpStep.AbilityScore -> hasValidAbilityScoreDecision()
            CharacterLevelUpStep.Spells -> preview.requirements
                .filterIsInstance<LevelUpRequirement.SpellDecisions>()
                .singleOrNull()
                ?.isComplete() == true &&
                !hasBlockingValidation(LevelUpValidationCode.SpellPolicy)
            CharacterLevelUpStep.Review -> false
        }

        private fun hasValidAbilityScoreDecision(): Boolean {
            val abilityIssueCodes = setOf(
                LevelUpValidationCode.AbilityScoreDecisionRequired,
                LevelUpValidationCode.InvalidAbilityScoreIncrease,
                LevelUpValidationCode.FeatRequired,
                LevelUpValidationCode.FeatPrerequisite,
                LevelUpValidationCode.FeatAlreadySelected,
                LevelUpValidationCode.UnsupportedFeatDecision,
                LevelUpValidationCode.MissingFeat,
                LevelUpValidationCode.ChoiceRequired,
                LevelUpValidationCode.InvalidChoice,
            )
            if (preview.validations.any {
                    it.severity == LevelUpValidationSeverity.Blocking && it.code in abilityIssueCodes
                }
            ) {
                return false
            }
            val requirement = preview.requirements
                .filterIsInstance<LevelUpRequirement.AbilityScoreImprovement>()
                .singleOrNull() ?: return false
            return when (val decision = requirement.selectedDecision) {
                is AbilityScoreDecision.Increase -> {
                    decision.increases.isNotEmpty() &&
                        decision.increases.keys.all { it in AbilityIds.standardOrder } &&
                        decision.increases.values.all { it > 0 } &&
                        decision.increases.values.sum() == requirement.abilityPoints &&
                        decision.increases.all { (ability, increase) ->
                            preview.before.abilityScores.scoreFor(ability) + increase <= requirement.maximumAbilityScore
                        }
                }
                is AbilityScoreDecision.Feat -> {
                    decision.featId in requirement.eligibleFeatIds &&
                        preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
                            .filter { it.category == LevelUpChoiceCategory.Feat }
                            .all { it.isComplete() }
                }
                null -> false
            }
        }

        private fun hasBlockingValidation(code: LevelUpValidationCode): Boolean =
            preview.validations.any {
                it.severity == LevelUpValidationSeverity.Blocking && it.code == code
            }
    }
}

private fun LevelUpRequirement.SpellDecisions.isComplete(): Boolean {
    val cantripIds = cantripCandidates.mapTo(hashSetOf()) { it.spellId }
    val knownIds = knownSpellCandidates.mapTo(hashSetOf()) { it.spellId }
    val spellbookIds = spellbookCandidates.mapTo(hashSetOf()) { it.spellId }
    val selectedCantrips = changes.learned.filter { it.classId == classId && it.spellId in cantripIds }
    val selectedKnown = changes.learned.filter { it.classId == classId && it.spellId in knownIds }
    val selectedSpellbook = changes.addedToSpellbook.filter { it.classId == classId && it.spellId in spellbookIds }
    val featureGrantsAreComplete = changes.featureLearned.keys == featureSpellGrants.map { it.featureId }.toSet() &&
        featureSpellGrants.all { grant ->
            val candidateIds = grant.candidates.mapTo(hashSetOf()) { it.spellId }
            val selected = changes.featureLearned[grant.featureId].orEmpty()
            selected.size == grant.requiredCount &&
                selected.all { it.classId == classId && it.spellId in candidateIds }
        }
    val completedReplacement = changes.replaced.singleOrNull()
    val replacementIsComplete = changes.replacementSourceSpellId == null &&
        changes.replaced.size <= 1 &&
        (completedReplacement == null || replacement?.let { requirement ->
            completedReplacement.classId == classId &&
                completedReplacement.removedSpellId in requirement.sourceCandidates.map { it.spellId } &&
                completedReplacement.learnedSpellId in requirement.replacementCandidates.map { it.spellId }
        } == true)
    return selectedCantrips.size == requiredCantripCount &&
        selectedKnown.size == requiredKnownSpellCount &&
        selectedSpellbook.size == requiredSpellbookAdditionCount &&
        featureGrantsAreComplete &&
        replacementIsComplete
}

private fun LevelUpRequirement.HitPoints.hasValidSelection(): Boolean = when (val gain = selectedGain) {
    is HitPointGain.Fixed -> gain.rolledValue == fixedGain
    is HitPointGain.Rolled -> gain.rolledValue in 1..hitDie
    is HitPointGain.Manual -> gain.rolledValue > 0
    null -> false
}

private fun LevelUpRequirement.ChoiceSelection.isComplete(): Boolean {
    val typedOptionIds = options.mapTo(hashSetOf(), LevelUpChoiceOption::id)
    val legalIds = if (typedOptionIds.isNotEmpty()) typedOptionIds else choice.optionIds()
    return selectedOptionIds.size == choice.choose &&
        (legalIds.isEmpty() || selectedOptionIds.all { it in legalIds })
}

private fun Choice.optionIds(): Set<String> = when (this) {
    is Choice.FavoredEnemyChoice -> from.toSet()
    is Choice.TerrainTypeChoice -> from.toSet()
    is Choice.ProficiencyChoice -> from.toSet()
    is Choice.FeatureChoice -> from.toSet()
    is Choice.OptionsArrayChoice -> from.toSet()
    is Choice.EquipmentChoice -> from.toSet()
    is Choice.AbilityBonusChoice -> from.flatMap { it.keys }.toSet()
    is Choice.NestedChoice -> from.flatMap { it.optionIds() }.toSet()
    is Choice.ResourceListChoice,
    is Choice.EquipmentCategoriesChoice,
    is Choice.FromAllChoice,
    is Choice.IdealChoice -> emptySet()
}

private fun com.github.arhor.spellbindr.domain.model.AbilityScores.scoreFor(abilityId: String): Int =
    when (abilityId) {
        AbilityIds.STR -> strength
        AbilityIds.DEX -> dexterity
        AbilityIds.CON -> constitution
        AbilityIds.INT -> intelligence
        AbilityIds.WIS -> wisdom
        AbilityIds.CHA -> charisma
        else -> Int.MIN_VALUE
    }