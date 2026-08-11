package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScorePrerequisite
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CasterContribution
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
<<<<<<< HEAD
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
=======
import com.github.arhor.spellbindr.domain.model.CharacterSpellPreparation
>>>>>>> ebb32f0 (feat: preserve prepared spells during level up)
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpClassEligibility
import com.github.arhor.spellbindr.domain.model.LevelUpDeferredFeatDecision
import com.github.arhor.spellbindr.domain.model.LevelUpFeatEligibility
import com.github.arhor.spellbindr.domain.model.LevelUpFeatureSpellGrantRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpHitDicePool
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpResource
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpSpellOption
import com.github.arhor.spellbindr.domain.model.LevelUpSpellReplacementRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.ProficiencyChoiceSelection
import com.github.arhor.spellbindr.domain.model.Prerequisite
import com.github.arhor.spellbindr.domain.model.SpellLearningPolicy
import com.github.arhor.spellbindr.domain.model.Spell
import javax.inject.Inject

/** Creates an empty, stale-safe one-level draft without inspecting a UI state. */
class CreateLevelUpPlanUseCase @Inject constructor() {
    operator fun invoke(progression: CharacterProgression): LevelUpPlan = LevelUpPlan(
        expectedTotalLevel = progression.totalLevel,
        rulesetId = progression.rulesetId,
        referenceDataVersion = progression.referenceDataVersion,
    )
}

/** Rebuilds a draft after every selection. All calculations are deterministic and side-effect free. */
class RebuildLevelUpPlanUseCase @Inject constructor() {
    operator fun invoke(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): LevelUpPreview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData)
}

/** Exposes validation without making callers duplicate the preview calculation. */
class ValidateLevelUpPlanUseCase @Inject constructor() {
    operator fun invoke(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): List<LevelUpValidationIssue> =
        LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData).validations
}

/**
 * Pure rules engine shared by the wizard and the eventual transactional materializer.
 *
 * Spell choices are derived from the selected class's own progression. Shared multiclass slot capacity never widens
 * a class spell list or the maximum spell level that class may learn.
 */
object LevelUpProgressionEngine {

    fun rebuild(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): LevelUpPreview {
        val validations = validateBaseState(sheet, progression, plan, referenceData).toMutableList()
        val selectedClass = plan.selectedClassId?.let(referenceData.classesById::get)
        val nextClassLevel = selectedClass?.let { progression.classLevels[it.id].orZero() + 1 }

        if (plan.selectedClassId == null) {
            validations += blocking(LevelUpValidationCode.ChoiceRequired, "Choose a class to level up.")
        } else if (selectedClass == null) {
            validations += blocking(LevelUpValidationCode.MissingClass, "The selected class no longer exists in reference data.")
        }
        if (progression.totalLevel >= LevelUpReferenceRules.maximumCharacterLevel) {
            validations += blocking(LevelUpValidationCode.MaximumCharacterLevel, "Maximum character level reached.")
        }
        if (nextClassLevel != null && nextClassLevel > LevelUpReferenceRules.maximumCharacterLevel) {
            validations += blocking(LevelUpValidationCode.MaximumClassLevel, "A class cannot exceed level 20.")
        }

        if (selectedClass != null && nextClassLevel != null) {
            validateClassSelection(sheet, progression, selectedClass, nextClassLevel, referenceData, validations)
            validateSelections(sheet, progression, plan, selectedClass, nextClassLevel, referenceData, validations)
        }

        val requirements = requirementsFor(
            abilities = sheet.abilityScores,
            hasKnownSpell = sheet.characterSpells.any { it.spellId.isNotBlank() },
            manualLanguageIds = manualLanguageIds(sheet.languages, referenceData),
            progression = progression,
            plan = plan,
            selectedClass = selectedClass,
            nextClassLevel = nextClassLevel,
            referenceData = referenceData,
            validations = validations,
        )
        val afterProgression = selectedClass?.let { clazz ->
            nextClassLevel?.let { classLevel ->
                progression.copy(levels = progression.levels + recordFor(plan, clazz, classLevel, progression, referenceData, validations))
            }
        } ?: progression
        val before = snapshot(sheet.abilityScores, progression, referenceData)
        val after = snapshot(applyAbilityDecision(sheet.abilityScores, plan.selections, referenceData), afterProgression, referenceData)
        validatePreparedSpells(sheet, after.abilityScores, afterProgression, referenceData, validations)
        return LevelUpPreview(before, after, requirements, validations.distinctBy {
            Triple(it.code, it.message, it.findingId)
        })
    }

    /** Creates the persisted record only after [rebuild] reports that confirmation is allowed. */
    fun recordFor(
        plan: LevelUpPlan,
        clazz: CharacterClass,
        classLevel: Int,
        progression: CharacterProgression,
        referenceData: LevelUpReferenceData,
        validations: List<LevelUpValidationIssue> = emptyList(),
    ): CharacterLevelRecord {
        val resolvedSubclass = plan.selections.subclassId ?: subclassFor(clazz.id, progression)
        val featureIds = activeFeatureChoices(clazz, classLevel, resolvedSubclass, referenceData)
            .mapTo(linkedSetOf()) { "${it.first.id}:choice" }
        val proficiencyIds = if (clazz.id !in progression.classLevels) {
            val multiClassing = clazz.multiClassing
            multiClassing?.proficiencyChoices.orEmpty().indices.mapTo(linkedSetOf()) {
                multiClassing?.proficiencyChoiceId(clazz.id, it).orEmpty()
            }
        } else {
            emptySet()
        }
        val selectedFeat = (plan.selections.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId
            ?.let(referenceData.featsById::get)
        val featIds = selectedFeat?.let(::activeFeatChoiceIds).orEmpty()
        val persistedPlan = when (selectedFeat?.id) {
            MAGIC_INITIATE_ID -> plan.takeIf { magicInitiateIsSupported(referenceData) }?.withMagicInitiateSpellGrant() ?: plan
            SPELL_SNIPER_ID -> plan.takeIf { spellSniperIsSupported(referenceData) }?.withSpellSniperSpellGrant() ?: plan
            else -> plan
        }
        return persistedPlan.toRecord(
            clazz = clazz,
            classLevel = classLevel,
            resolvedSubclassId = resolvedSubclass,
            activeFeatureChoiceIds = featureIds,
            activeProficiencyChoiceIds = proficiencyIds,
            activeFeatChoiceIds = featIds,
            activeAcknowledgementIds = validations.filter { it.severity == LevelUpValidationSeverity.Overrideable }
                .mapTo(linkedSetOf(), LevelUpValidationIssue::acknowledgementId),
        )
    }

    private fun validateBaseState(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        data: LevelUpReferenceData,
    ): List<LevelUpValidationIssue> = buildList {
        if (sheet.level != progression.totalLevel || plan.expectedTotalLevel != progression.totalLevel) {
            add(blocking(LevelUpValidationCode.StaleProgression, "The character changed; reload the level-up draft."))
        }
        if (progression.rulesetId != LevelUpReferenceRules.rulesetId || plan.rulesetId != progression.rulesetId) {
            add(blocking(LevelUpValidationCode.UnsupportedRuleset, "This progression uses an unsupported ruleset."))
        }
        if (progression.referenceDataVersion != data.referenceDataVersion ||
            plan.referenceDataVersion != progression.referenceDataVersion
        ) {
            add(blocking(LevelUpValidationCode.ReferenceDataVersionMismatch, "Reference data changed; reload the draft."))
        }
        progressionIntegrityIssue(progression, data)?.let(::add)
    }

    private fun progressionIntegrityIssue(
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): LevelUpValidationIssue? {
        val counts = mutableMapOf<String, Int>()
        progression.levels.forEachIndexed { index, record ->
            val clazz = data.classesById[record.classId]
                ?: return blocking(LevelUpValidationCode.MissingClass, "Progression references missing class ${record.classId}.")
            val expectedClassLevel = counts.getOrDefault(record.classId, 0) + 1
            if (record.characterLevel != index + 1 || record.classLevel != expectedClassLevel) {
                return blocking(LevelUpValidationCode.CorruptProgression, "Progression level order is corrupt.")
            }
            if (clazz.levels.none { it.level == record.classLevel }) {
                return blocking(LevelUpValidationCode.MissingClassLevel, "Class level data is missing for ${clazz.name}.")
            }
            record.subclassId?.let { subclassId ->
                if (clazz.subclasses.none { it.id == subclassId }) {
                    return blocking(LevelUpValidationCode.MissingSubclass, "Progression references missing subclass $subclassId.")
                }
            }
            counts[record.classId] = expectedClassLevel
        }
        return null
    }

    private fun validateClassSelection(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        clazz: CharacterClass,
        nextClassLevel: Int,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        if (clazz.levels.none { it.level == nextClassLevel }) {
            validations += blocking(LevelUpValidationCode.MissingClassLevel, "Class level data is missing for ${clazz.name}.")
        }
        val isNewClass = clazz.id !in progression.classLevels
        if (isNewClass) {
            (progression.classLevels.keys + clazz.id).sorted().forEach { classId ->
                val classRule = data.classesById[classId]?.multiClassing
                if (classRule == null) {
                    validations += blocking(LevelUpValidationCode.MissingClass, "Multiclass rules are missing for $classId.")
                } else if (!meetsPrerequisites(sheet.abilityScores, classRule.prerequisites)) {
                    validations += overrideable(
                        LevelUpValidationCode.MulticlassPrerequisite,
                        "Ability prerequisites for ${data.classesById[classId]?.name ?: classId} are not met.",
                        findingId = "multiclass-prerequisite:$classId",
                    )
                }
            }
        }
        val targetLevel = progression.totalLevel + 1
        val experience = sheet.experiencePoints
        val threshold = LevelUpReferenceRules.experienceThresholds[targetLevel]
        if (experience != null && threshold != null && experience < threshold) {
            validations += overrideable(
                LevelUpValidationCode.ExperienceThreshold,
                "${threshold - experience} more XP is normally required for level $targetLevel.",
                findingId = "experience-threshold:$targetLevel",
            )
        }
    }

    private fun validateSelections(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        clazz: CharacterClass,
        nextClassLevel: Int,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val selections = plan.selections
        val existingSubclass = subclassFor(clazz.id, progression)
        val requiresSubclass = LevelUpReferenceRules.policyFor(clazz.id)?.subclass?.level == nextClassLevel &&
            existingSubclass == null
        if (requiresSubclass && plan.selections.subclassId == null) {
            validations += blocking(LevelUpValidationCode.SubclassRequired, "Choose a ${clazz.name} subclass.")
        }
        if (selections.subclassId != null && existingSubclass == null && !requiresSubclass) {
            validations += blocking(LevelUpValidationCode.StickySubclass, "A subclass cannot be selected before its acquisition level.")
        }
        val proposedSubclass = selections.subclassId ?: existingSubclass
        if (selections.subclassId != null && clazz.subclasses.none { it.id == selections.subclassId }) {
            validations += blocking(LevelUpValidationCode.MissingSubclass, "The selected subclass does not belong to ${clazz.name}.")
        }
        if (existingSubclass != null && selections.subclassId != null && existingSubclass != selections.subclassId) {
            validations += blocking(LevelUpValidationCode.StickySubclass, "A class subclass cannot be changed while leveling up.")
        }

        activeFeatureChoices(clazz, nextClassLevel, proposedSubclass, data).forEach { (feature, choice) ->
            validateChoice(
                id = "${feature.id}:choice",
                choice = choice,
                selected = selections.featureChoices["${feature.id}:choice"].orEmpty(),
                label = feature.name,
                validations = validations,
            )
        }
        if (clazz.id !in progression.classLevels) {
            clazz.multiClassing?.let { multiClassing -> multiClassing.proficiencyChoices.forEachIndexed { index, choice ->
                val id = multiClassing.proficiencyChoiceId(clazz.id, index)
                validateChoice(id, choice, selections.proficiencyChoices[id].orEmpty(), "${clazz.name} proficiency", validations)
            } }
        }
        validateHitPointGain(
            gain = selections.hitPointGain,
            hitDie = clazz.hitDie,
            isFirstCharacterLevel = progression.totalLevel == 0,
            validations = validations,
        )
        val asiPolicy = LevelUpReferenceRules.policyFor(clazz.id)?.abilityScoreImprovement
        if (asiPolicy?.levels?.contains(nextClassLevel) == true) {
            val before = snapshot(sheet.abilityScores, progression, data)
            val ownedProficiencyIds = before.proficiencyIds + before.savingThrowAbilityIds.map {
                "$SAVING_THROW_PREFIX$it"
            }
            validateAbilityDecision(
                sheet.abilityScores,
                sheet.characterSpells.any { it.spellId.isNotBlank() },
                ownedProficiencyIds,
                before.languageIds + manualLanguageIds(sheet.languages, data),
                progression,
                selections,
                asiPolicy.abilityPoints,
                asiPolicy.maximumAbilityScore,
                asiPolicy.allowsFeat,
                data,
                validations,
            )
        }
        validateSpellChanges(
            progression,
            selections.spellChanges,
            clazz,
            nextClassLevel,
            proposedSubclass,
            data,
            validations,
        )
    }

    private fun validateHitPointGain(
        gain: HitPointGain?,
        hitDie: Int,
        isFirstCharacterLevel: Boolean,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        if (gain == null) {
            validations += blocking(LevelUpValidationCode.HitPointGainRequired, "Choose a hit point gain.")
            return
        }
        val expectedFixed = if (isFirstCharacterLevel) hitDie else hitDie / 2 + 1
        val isValid = when (gain) {
            is HitPointGain.Fixed -> gain.rolledValue == expectedFixed
            is HitPointGain.Rolled -> gain.rolledValue in 1..hitDie
            is HitPointGain.Manual -> gain.rolledValue >= 1
        }
        if (!isValid) validations += blocking(LevelUpValidationCode.InvalidHitPointGain, "The hit point gain is invalid.")
        if (gain is HitPointGain.Manual && isValid) {
            validations += LevelUpValidationIssue(
                code = LevelUpValidationCode.ManualHitPointGainOverride,
                message = "Manual hit point gain of ${gain.rolledValue} overrides the rules-derived fixed gain of " +
                    "$expectedFixed or a rolled result from 1 to $hitDie.",
                severity = LevelUpValidationSeverity.Overrideable,
                findingId = "${LevelUpValidationCode.ManualHitPointGainOverride.name}:${gain.rolledValue}",
            )
        }
    }

    private fun validateAbilityDecision(
        abilities: AbilityScores,
        hasKnownSpell: Boolean,
        proficiencyIds: Set<String>,
        languageIds: Set<String>,
        progression: CharacterProgression,
        selections: LevelUpSelections,
        points: Int,
        maximum: Int,
        allowsFeat: Boolean,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        when (val decision = selections.abilityScoreDecision) {
            null -> validations += blocking(LevelUpValidationCode.AbilityScoreDecisionRequired, "Choose an ability score improvement or feat.")
            is AbilityScoreDecision.Increase -> {
                val validIds = decision.increases.keys.all { it in AbilityIds.standardOrder }
                val validPoints = decision.increases.values.all { it > 0 } && decision.increases.values.sum() == points
                val withinCap = decision.increases.all { (ability, amount) -> abilityScore(abilities, ability) + amount <= maximum }
                if (!validIds || !validPoints || !withinCap) {
                    validations += blocking(LevelUpValidationCode.InvalidAbilityScoreIncrease, "Ability score increases must use $points point(s) without exceeding $maximum.")
                }
            }
            is AbilityScoreDecision.Feat -> {
                if (!allowsFeat) {
                    validations += blocking(LevelUpValidationCode.FeatRequired, "This class level does not allow a feat.")
                }
                val feat = data.featsById[decision.featId]
                if (feat == null) {
                    validations += blocking(LevelUpValidationCode.MissingFeat, "The selected feat no longer exists in reference data.")
                } else {
                    val alreadySelected = progression.levels.any { record ->
                        (record.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId == feat.id
                    }
                    if (alreadySelected && !feat.repeatable) {
                        validations += blocking(
                            LevelUpValidationCode.FeatAlreadySelected,
                            "${feat.name} cannot be selected more than once.",
                        )
                    }
                    val deferredDecision = deferredFeatDecision(feat.id)
                        ?.takeUnless {
                            (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(data)) ||
                                (feat.id == SPELL_SNIPER_ID && spellSniperIsSupported(data))
                        }
                        ?.takeUnless { feat.id == MARTIAL_ADEPT_ID && feat.maneuverChoice != null }
                    if (deferredDecision != null) {
                        validations += blocking(
                            LevelUpValidationCode.UnsupportedFeatDecision,
                            deferredFeatReason(deferredDecision),
                        )
                    }
                    if (!meetsFeatPrerequisites(
                            abilities,
                            proficiencyIds,
                            hasKnownSpell,
                            progression,
                            data,
                            feat.prerequisites,
                        )
                    ) {
                        validations += blocking(LevelUpValidationCode.FeatPrerequisite, "Prerequisites for ${feat.name} are not met.")
                    }
                    if (feat.correlatesAbilityAndSavingThrow) {
                        validateCorrelatedAbilitySavingThrowChoice(
                            feat = feat,
                            selections = selections,
                            abilities = abilities,
                            proficiencyIds = proficiencyIds,
                            maximum = maximum,
                            validations = validations,
                        )
                    } else feat.abilityBonusChoice?.let { choice ->
                        val choiceId = feat.abilityBonusChoiceId.orEmpty()
                        validateFeatChoice(
                            choiceId,
                            choice,
                            selections.featChoices[choiceId].orEmpty(),
                            abilities,
                            feat.name,
                            validations,
                        )
                    }
                    feat.languageChoice?.let { choice ->
                        val choiceId = feat.languageChoiceId.orEmpty()
                        validateChoice(
                            id = choiceId,
                            choice = choice,
                            selected = selections.featChoices[choiceId].orEmpty(),
                            label = feat.name,
                            validations = validations,
                            legalOptionIds = data.languagesById.keys - languageIds,
                        )
                    }
                    if (!feat.correlatesAbilityAndSavingThrow) feat.proficiencyChoice?.let { choice ->
                        val choiceId = feat.proficiencyChoiceId.orEmpty()
                        validateChoice(
                            id = choiceId,
                            choice = choice,
                            selected = selections.featChoices[choiceId].orEmpty(),
                            label = feat.name,
                            validations = validations,
                            legalOptionIds = choice.from.toSet() - proficiencyIds,
                        )
                    }
                    feat.damageTypeChoice?.let { choice ->
                        val choiceId = feat.damageTypeChoiceId.orEmpty()
                        validateChoice(
                            id = choiceId,
                            choice = choice,
                            selected = selections.featChoices[choiceId].orEmpty(),
                            label = feat.name,
                            validations = validations,
                            legalOptionIds = legalDamageTypes(feat, progression),
                        )
                    }
                    feat.maneuverChoice?.let { choice ->
                        val choiceId = feat.maneuverChoiceId.orEmpty()
                        validateChoice(
                            id = choiceId,
                            choice = choice,
                            selected = selections.featChoices[choiceId].orEmpty(),
                            label = feat.name,
                            validations = validations,
                            legalOptionIds = choice.from.toSet(),
                        )
                    }
                    if (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(data)) {
                        validateMagicInitiateSelections(selections, data, validations)
                    }
                    if (feat.id == SPELL_SNIPER_ID && spellSniperIsSupported(data)) {
                        validateSpellSniperSelections(selections, data, validations)
                    }
                    val afterFeat = applyAbilityDecision(abilities, selections, data)
                    if (AbilityIds.standardOrder.any { abilityScore(afterFeat, it) > 20 }) {
                        validations += blocking(LevelUpValidationCode.InvalidAbilityScoreIncrease, "A feat cannot increase an ability score above 20.")
                    }
                }
            }
        }
    }

    /** Validates selections against the selected class's own spell progression, never multiclass slot level. */
    private fun validateSpellChanges(
        progression: CharacterProgression,
        changes: com.github.arhor.spellbindr.domain.model.SpellChanges,
        clazz: CharacterClass,
        classLevel: Int,
        subclassId: String?,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val policy = LevelUpReferenceRules.policyFor(clazz.id)?.spells
        if (policy == null || policy == SpellLearningPolicy.None) {
            if (changes != com.github.arhor.spellbindr.domain.model.SpellChanges()) {
                validations += blocking(
                    LevelUpValidationCode.SpellPolicy,
                    "The selected class does not allow spell decisions at this level.",
                )
            }
            return
        }
        val featureRequirements = magicalSecretFeatures(clazz, classLevel, subclassId, data)
        val featureRefs = changes.featureLearned.values.flatten()
        val previouslyOwnedSpellIds = classOwnedSpellIds(progression, clazz.id)
        val allRefs = changes.learned + changes.addedToSpellbook + featureRefs +
            changes.replaced.flatMapTo(linkedSetOf()) {
            setOf(com.github.arhor.spellbindr.domain.model.ClassSpellRef(it.classId, it.removedSpellId),
                com.github.arhor.spellbindr.domain.model.ClassSpellRef(it.classId, it.learnedSpellId))
        }
        if (allRefs.any { it.classId != clazz.id }) {
            validations += blocking(LevelUpValidationCode.SpellPolicy, "Spell decisions must belong to ${clazz.name}.")
            return
        }
        val classSpellcasting = clazz.levels.firstOrNull { it.level == classLevel }?.spellcasting
        val previousSpellcasting = clazz.levels.firstOrNull { it.level == classLevel - 1 }?.spellcasting
        val maximumSpellLevel = maximumSpellLevel(classSpellcasting)
        fun spell(ref: com.github.arhor.spellbindr.domain.model.ClassSpellRef) = data.spellsById[ref.spellId]
        val ordinaryGrantedRefs = changes.learned + changes.addedToSpellbook + changes.replaced.map { replacement ->
            com.github.arhor.spellbindr.domain.model.ClassSpellRef(replacement.classId, replacement.learnedSpellId)
        }
        val replacementSourceRefs = changes.replaced.map { replacement ->
            com.github.arhor.spellbindr.domain.model.ClassSpellRef(replacement.classId, replacement.removedSpellId)
        }
        val invalid = ordinaryGrantedRefs.any { ref ->
            val value = spell(ref)
            value == null || clazz.id !in value.classes.map { it.id } ||
                value.level > maximumSpellLevel
        } || replacementSourceRefs.any { ref ->
            val value = spell(ref)
            value == null || ref.spellId !in previouslyOwnedSpellIds || value.level > maximumSpellLevel
        } || featureRefs.any { ref ->
            val value = spell(ref)
            value == null || value.level > maximumSpellLevel || ref.spellId in previouslyOwnedSpellIds
        }
        if (invalid) {
            validations += blocking(
                LevelUpValidationCode.SpellPolicy,
                "A selected spell is not legal for this class level.",
            )
        }
        val selectedCantrips = changes.learned.count { spell(it)?.level == 0 }
        val selectedKnown = changes.learned.count { spell(it)?.level?.let { level -> level > 0 } == true }
        val expectedCantrips = (
            classSpellcasting?.cantrips.orZero() - previousSpellcasting?.cantrips.orZero()
        ).coerceAtLeast(0)
        val expectedFeatureIds = featureRequirements.mapTo(hashSetOf()) { it.first }
        val featureSelectionsAreInvalid = changes.featureLearned.keys != expectedFeatureIds ||
            featureRequirements.any { (featureId, _) -> changes.featureLearned[featureId].orEmpty().size != 2 }
        if (featureSelectionsAreInvalid) {
            validations += blocking(
                LevelUpValidationCode.SpellPolicy,
                "Choose exactly two spells for each Magical Secrets feature gained at this level.",
            )
        }
        val allNewSpellIds = changes.learned.map { it.spellId } +
            changes.addedToSpellbook.map { it.spellId } +
            featureRefs.map { it.spellId } +
            changes.replaced.map { it.learnedSpellId }
        if (allNewSpellIds.distinct().size != allNewSpellIds.size) {
            validations += blocking(LevelUpValidationCode.SpellPolicy, "Each newly granted spell must be distinct.")
        }
        when (policy) {
            is SpellLearningPolicy.Known -> {
                val classMagicalSecretCount = featureRequirements.count { it.first in BARD_MAGICAL_SECRETS } * 2
                val expectedKnown = ((
                    classSpellcasting?.spells.orZero() - previousSpellcasting?.spells.orZero()
                ).coerceAtLeast(0) - classMagicalSecretCount).coerceAtLeast(0)
                if (selectedCantrips != expectedCantrips || selectedKnown != expectedKnown) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "Choose the required known spells and cantrips for this class level.",
                    )
                }
                if (changes.addedToSpellbook.isNotEmpty() || changes.replaced.size > 1 ||
                    (changes.replaced.isNotEmpty() && classLevel < policy.replacementStartsAtLevel)
                ) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "The selected spell replacement is not allowed at this class level.",
                    )
                }
                val currentlyKnown = classOwnedSpellIds(progression, clazz.id)
                val removed = changes.replaced.map { it.removedSpellId }
                val replacements = changes.replaced.map { it.learnedSpellId }
                val learned = changes.learned.map { it.spellId }
                if (removed.distinct().size != removed.size || replacements.distinct().size != replacements.size ||
                    removed.any { it !in currentlyKnown || data.spellsById[it]?.level == 0 } ||
                    changes.replaced.any { it.removedSpellId == it.learnedSpellId } ||
                    replacements.any { data.spellsById[it]?.level == 0 } ||
                    replacements.any { it in currentlyKnown && it !in removed } ||
                    (replacements + learned).distinct().size != replacements.size + learned.size ||
                    learned.any { it in currentlyKnown && it !in removed }
                ) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "Replacements must remove a currently known class spell and learn a distinct, " +
                            "non-duplicate spell.",
                    )
                }
                if (changes.replacementSourceSpellId != null) {
                    val sourceIsLegal = classLevel >= policy.replacementStartsAtLevel &&
                        changes.replacementSourceSpellId in currentlyKnown &&
                        data.spellsById[changes.replacementSourceSpellId]?.level?.let { it > 0 } == true
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        if (sourceIsLegal) {
                            "Choose the replacement spell or clear the optional replacement."
                        } else {
                            "The selected spell replacement source is not legal for this class level."
                        },
                    )
                }
            }
            is SpellLearningPolicy.Prepared -> {
                if (selectedCantrips != expectedCantrips || selectedKnown != 0 ||
                    changes.addedToSpellbook.isNotEmpty() ||
                    changes.replaced.isNotEmpty() || changes.replacementSourceSpellId != null ||
                    changes.featureLearned.isNotEmpty()
                ) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "Prepared casters only choose newly gained cantrips here.",
                    )
                }
            }
            is SpellLearningPolicy.Spellbook -> {
                val expectedBook = if (classLevel == 1) policy.spellsAtFirstLevel else policy.spellsAddedPerLevel
                if (selectedCantrips != expectedCantrips || selectedKnown != 0 ||
                    changes.addedToSpellbook.size != expectedBook || changes.replaced.isNotEmpty() ||
                    changes.replacementSourceSpellId != null || changes.featureLearned.isNotEmpty()
                ) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "Choose the required spellbook additions and cantrips for this class level.",
                    )
                }
                if (changes.addedToSpellbook.any { spell(it)?.level == 0 }) {
                    validations += blocking(LevelUpValidationCode.SpellPolicy, "Cantrips are not spellbook additions.")
                }
            }
            SpellLearningPolicy.None -> Unit
        }
    }

    private fun requirementsFor(
        abilities: AbilityScores,
        hasKnownSpell: Boolean,
        manualLanguageIds: Set<String>,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        selectedClass: CharacterClass?,
        nextClassLevel: Int?,
        referenceData: LevelUpReferenceData,
        validations: List<LevelUpValidationIssue>,
    ): List<LevelUpRequirement> = buildList {
        val eligibility = referenceData.classes.sortedBy { it.id }.map { candidate ->
            classEligibility(abilities, progression, candidate, referenceData)
        }
        add(LevelUpRequirement.ClassSelection(
            eligibleClassIds = eligibility.filter(LevelUpClassEligibility::eligible).map(LevelUpClassEligibility::classId),
            selectedClassId = plan.selectedClassId,
            eligibility = eligibility,
        ))
        if (selectedClass == null || nextClassLevel == null) return@buildList
        val existingSubclass = subclassFor(selectedClass.id, progression)
        if (LevelUpReferenceRules.policyFor(selectedClass.id)?.subclass?.level == nextClassLevel && existingSubclass == null) {
            add(LevelUpRequirement.SubclassSelection(
                id = "${selectedClass.id}:subclass",
                classId = selectedClass.id,
                options = selectedClass.subclasses.map { LevelUpChoiceOption(it.id, it.name) }.sortedBy { it.id },
                selectedSubclassId = plan.selections.subclassId,
            ))
        }
        val subclass = plan.selections.subclassId ?: existingSubclass
        activeFeatureChoices(selectedClass, nextClassLevel, subclass, referenceData).forEach { (feature, choice) ->
            val id = "${feature.id}:choice"
            add(LevelUpRequirement.ChoiceSelection(id, feature.id, feature.name, choice,
                plan.selections.featureChoices[id].orEmpty(), LevelUpChoiceCategory.Feature))
        }
        if (selectedClass.id !in progression.classLevels) selectedClass.multiClassing?.let { multiClassing -> multiClassing.proficiencyChoices
            .forEachIndexed { index, choice ->
                val id = multiClassing.proficiencyChoiceId(selectedClass.id, index)
                add(LevelUpRequirement.ChoiceSelection(id, selectedClass.id, "${selectedClass.name} proficiency", choice,
                    plan.selections.proficiencyChoices[id].orEmpty(), LevelUpChoiceCategory.Proficiency))
            } }
        add(LevelUpRequirement.HitPoints(
            hitDie = selectedClass.hitDie,
            fixedGain = if (progression.totalLevel == 0) selectedClass.hitDie else selectedClass.hitDie / 2 + 1,
            selectedGain = plan.selections.hitPointGain,
        ))
        LevelUpReferenceRules.policyFor(selectedClass.id)?.let { policy ->
            if (nextClassLevel in policy.abilityScoreImprovement.levels) {
                val asiPolicy = policy.abilityScoreImprovement
                val before = snapshot(abilities, progression, referenceData)
                val proficiencyIds = before.proficiencyIds + before.savingThrowAbilityIds.map {
                    "$SAVING_THROW_PREFIX$it"
                }
                val languageIds = before.languageIds + manualLanguageIds
                val featEligibility = referenceData.feats.sortedBy { it.id }.map { feat ->
                    featEligibilityFor(
                        feat = feat,
                        abilities = abilities,
                        maximum = asiPolicy.maximumAbilityScore,
                        proficiencyIds = proficiencyIds,
                        languageIds = languageIds,
                        hasKnownSpell = hasKnownSpell,
                        progression = progression,
                        data = referenceData,
                    )
                }
                val eligibleFeatIds = if (asiPolicy.allowsFeat) {
                    featEligibility.filter(LevelUpFeatEligibility::eligible).map(LevelUpFeatEligibility::featId)
                } else emptyList()
                add(LevelUpRequirement.AbilityScoreImprovement(
                    id = "${selectedClass.id}:$nextClassLevel:asi",
                    classId = selectedClass.id,
                    abilityPoints = asiPolicy.abilityPoints,
                    maximumAbilityScore = asiPolicy.maximumAbilityScore,
                    allowsFeat = asiPolicy.allowsFeat,
                    eligibleFeatIds = eligibleFeatIds,
                    featEligibility = featEligibility,
                    selectedDecision = plan.selections.abilityScoreDecision,
                ))
                val selectedFeat = (plan.selections.abilityScoreDecision as? AbilityScoreDecision.Feat)
                    ?.featId
                    ?.let(referenceData.featsById::get)
                selectedFeat?.let { feat ->
                    featOwnedChoiceRequirements(
                        feat = feat,
                        abilities = abilities,
                        maximum = asiPolicy.maximumAbilityScore,
                        proficiencyIds = proficiencyIds,
                        languageIds = languageIds,
                        progression = progression,
                        data = referenceData,
                    ).forEach { requirement ->
                    add(LevelUpRequirement.ChoiceSelection(
                        id = requirement.id,
                        sourceId = feat.id,
                        label = feat.name,
                        choice = requirement.choice,
                        selectedOptionIds = plan.selections.featChoices[requirement.id].orEmpty(),
                        category = LevelUpChoiceCategory.Feat,
                        options = requirement.options,
                    ))
                    }
                    if (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(referenceData)) {
                        addAll(magicInitiateChoiceRequirements(referenceData, plan.selections.featChoices))
                    }
                    if (feat.id == SPELL_SNIPER_ID && spellSniperIsSupported(referenceData)) {
                        addAll(spellSniperChoiceRequirements(referenceData, plan.selections.featChoices))
                    }
                }
            }
            val currentSpellcasting = selectedClass.levels.firstOrNull { it.level == nextClassLevel }?.spellcasting
            val hasSpellcastingCapacity = currentSpellcasting?.let { spellcasting ->
                spellcasting.cantrips.orZero() > 0 ||
                    spellcasting.spells.orZero() > 0 ||
                    spellcasting.spellSlots.orEmpty().values.any { it > 0 }
            } == true
            if (policy.spells != SpellLearningPolicy.None && hasSpellcastingCapacity) {
                add(spellRequirementFor(
                    clazz = selectedClass,
                    classLevel = nextClassLevel,
                    policy = policy.spells,
                    subclassId = subclass,
                    progression = progression,
                    changes = plan.selections.spellChanges,
                    preparationCapacity = preparationCapacity(
                        policy.spells,
                        nextClassLevel,
                        abilities,
                        plan,
                        referenceData,
                    ),
                    data = referenceData,
                ))
            }
        }
        validations.filter { it.severity == LevelUpValidationSeverity.Overrideable }.forEach { issue ->
            add(LevelUpRequirement.Acknowledgement(issue.acknowledgementId, issue,
                issue.acknowledgementId in plan.selections.acknowledgedIssueCodes))
        }
    }

    private fun snapshot(
        abilities: AbilityScores,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): LevelUpSnapshot {
        val classesById = data.classesById
        val classLevels = progression.classLevels
        val pools = progression.levels.groupBy { record -> classesById[record.classId]?.hitDie ?: 0 }
            .filterKeys { it > 0 }.map { (die, records) -> LevelUpHitDicePool(die, records.size) }.sortedBy { it.dieSize }
        val firstClass = progression.levels.firstOrNull()?.classId
        val proficiencyIds = linkedSetOf<String>()
        val savingThrows = linkedSetOf<AbilityId>()
        val featureIds = linkedSetOf<String>()
        val languageIds = linkedSetOf<String>()
        val featManeuvers = linkedMapOf<String, MutableSet<String>>()
        var martialAdeptCount = 0
        progression.levels.forEachIndexed { index, record ->
            val clazz = classesById[record.classId] ?: return@forEachIndexed
            if (index == 0) {
                proficiencyIds += clazz.proficiencies
                savingThrows += clazz.savingThrows
            } else if (record.classLevel == 1) {
                proficiencyIds += clazz.multiClassing?.proficiencyGrants.orEmpty()
            }
            proficiencyIds += record.proficiencyChoices.flatMap { it.selectedProficiencyIds }
            val gainedFeatureIds = buildList {
                addAll(clazz.levels.firstOrNull { it.level == record.classLevel }?.features.orEmpty())
                record.subclassId?.let { subclassId ->
                    addAll(
                        clazz.subclasses.firstOrNull { it.id == subclassId }
                            ?.levels?.firstOrNull { it.level == record.classLevel }
                            ?.features.orEmpty(),
                    )
                }
            }
            featureIds += gainedFeatureIds
            gainedFeatureIds.forEach { featureId ->
                val choice = data.featuresById[featureId]?.choice as? Choice.ProficiencyChoice
                    ?: return@forEach
                val selected = record.featureChoices["$featureId:choice"]
                    ?: record.featureChoices[featureId]
                    ?: emptySet()
                selected.filter { it in choice.from }.forEach { proficiencyId ->
                    val ability = proficiencyId.removePrefix(SAVING_THROW_PREFIX)
                    if (proficiencyId.startsWith(SAVING_THROW_PREFIX) && ability in AbilityIds.standardOrder) {
                        savingThrows += ability
                    } else {
                        proficiencyIds += proficiencyId
                    }
                }
            }
            val feat = (record.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId?.let(data.featsById::get)
            feat?.maneuverChoiceId?.let { choiceId ->
                val selected = record.featChoices[choiceId].orEmpty()
                if (selected.isNotEmpty()) featManeuvers.getOrPut(feat.id) { linkedSetOf() } += selected
                if (feat.id == MARTIAL_ADEPT_ID) martialAdeptCount++
            }
            feat?.effects?.filterIsInstance<Effect.AddProficienciesEffect>()
                ?.flatMap { it.proficiencies }
                ?.forEach { proficiencyId ->
                    val ability = proficiencyId.removePrefix(SAVING_THROW_PREFIX)
                    if (proficiencyId.startsWith(SAVING_THROW_PREFIX) && ability in AbilityIds.standardOrder) {
                        savingThrows += ability
                    } else {
                        proficiencyIds += proficiencyId
                    }
                }
            feat?.proficiencyChoiceId?.takeUnless { feat.correlatesAbilityAndSavingThrow }?.let { choiceId ->
                val selected = record.featChoices[choiceId].orEmpty()
                selected.forEach { proficiencyId ->
                    val ability = proficiencyId.removePrefix(SAVING_THROW_PREFIX)
                    if (proficiencyId.startsWith(SAVING_THROW_PREFIX) && ability in AbilityIds.standardOrder) {
                        savingThrows += ability
                    } else {
                        proficiencyIds += proficiencyId
                    }
                }
            }
            feat?.correlatedAbilitySavingThrowChoiceId?.let { choiceId ->
                record.featChoices[choiceId].orEmpty().filterTo(savingThrows) { it in AbilityIds.standardOrder }
            }
            feat?.languageChoiceId?.let { choiceId ->
                languageIds += record.featChoices[choiceId].orEmpty()
            }
        }
        val sharedCasterLevel = classLevels.entries.sumOf { (classId, level) -> when (casterContributionFor(classId, progression)) {
            CasterContribution.Full -> level
            CasterContribution.Half -> level / 2
            CasterContribution.Third -> level / 3
            else -> 0
        } }
        val warlockLevel = classLevels[LevelUpReferenceRules.pactMagic.classId].orZero()
        val pactMagic = warlockLevel.takeIf { it > 0 }?.let { level ->
            LevelUpPactMagicCapacity(
                slotLevel = when (level) { in 1..2 -> 1; in 3..4 -> 2; in 5..6 -> 3; in 7..8 -> 4; else -> 5 },
                slots = when (level) { 1 -> 1; in 2..10 -> 2; in 11..16 -> 3; else -> 4 },
            )
        }
        val featHitPoints = progression.levels.sumOf { record ->
            val feat = (record.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId?.let(data.featsById::get)
            feat?.effects?.filterIsInstance<Effect.AddHpEffect>().orEmpty().sumOf { effect ->
                effect.value * if (effect.perLevel) progression.totalLevel else 1
            }
        }
        val maxHp = (progression.levels.sumOf { it.hitPointGain.rolledValue } +
            progression.totalLevel * abilities.modifierFor(AbilityIds.CON) + featHitPoints).coerceAtLeast(1)
        return LevelUpSnapshot(
            totalLevel = progression.totalLevel,
            classLevels = classLevels.toSortedMap(),
            classDisplayName = progression.levels.map { it.classId }.distinct()
                .joinToString(" / ") { id -> "${classesById[id]?.name ?: id} ${classLevels[id]}" },
            proficiencyBonus = 2 + ((progression.totalLevel.coerceAtLeast(1) - 1) / 4),
            abilityScores = abilities,
            maximumHitPoints = maxHp,
            hitDicePools = pools,
            proficiencyIds = proficiencyIds,
            savingThrowAbilityIds = savingThrows,
            featureIds = featureIds,
            sharedCasterLevel = sharedCasterLevel,
            sharedSpellSlots = LevelUpReferenceRules.sharedSpellSlots[sharedCasterLevel].orEmpty(),
            pactMagic = pactMagic,
            languageIds = languageIds,
            featManeuvers = featManeuvers.mapValues { it.value.toSet() },
            resources = if (martialAdeptCount > 0) listOf(
                LevelUpResource(
                    id = SUPERIORITY_DIE_RESOURCE_ID,
                    name = "Superiority dice",
                    maximum = martialAdeptCount,
                ),
            ) else emptyList(),
        )
    }

    private fun LevelUpPlan.toRecord(
        clazz: CharacterClass,
        classLevel: Int,
        resolvedSubclassId: String?,
        activeFeatureChoiceIds: Set<String>,
        activeProficiencyChoiceIds: Set<String>,
        activeFeatChoiceIds: Set<String>,
        activeAcknowledgementIds: Set<String>,
    ): CharacterLevelRecord = CharacterLevelRecord(
        characterLevel = expectedTotalLevel + 1,
        classId = clazz.id,
        classLevel = classLevel,
        subclassId = resolvedSubclassId,
        hitPointGain = selections.hitPointGain ?: HitPointGain.Fixed(0),
        featureChoices = selections.featureChoices.filterKeys(activeFeatureChoiceIds::contains).toSortedMap(),
        proficiencyChoices = selections.proficiencyChoices.filterKeys(activeProficiencyChoiceIds::contains).toSortedMap().map { (id, selected) ->
            ProficiencyChoiceSelection(id, selected.toSortedSet())
        },
        abilityScoreDecision = selections.abilityScoreDecision,
        featChoices = selections.featChoices.filterKeys(activeFeatChoiceIds::contains).toSortedMap(),
        spellChanges = selections.spellChanges,
        ruleAcknowledgements = selections.acknowledgedIssueCodes.filterTo(sortedSetOf(), activeAcknowledgementIds::contains),
        notes = selections.note?.takeIf(String::isNotBlank),
    )

    private fun activeFeatureChoices(
        clazz: CharacterClass,
        classLevel: Int,
        subclassId: String?,
        data: LevelUpReferenceData,
    ): List<Pair<Feature, Choice>> = buildList {
        clazz.levels.firstOrNull { it.level == classLevel }?.features.orEmpty().forEach { featureId ->
            data.featuresById[featureId]?.choice?.let { add(data.featuresById.getValue(featureId) to it) }
        }
        subclassId?.let { id -> clazz.subclasses.firstOrNull { it.id == id }?.levels
            ?.firstOrNull { it.level == classLevel }?.features.orEmpty().forEach { featureId ->
                data.featuresById[featureId]?.choice?.let { add(data.featuresById.getValue(featureId) to it) }
            } }
    }.sortedBy { it.first.id }

    private fun validateChoice(
        id: String,
        choice: Choice,
        selected: Set<String>,
        label: String,
        validations: MutableList<LevelUpValidationIssue>,
        legalOptionIds: Set<String>? = null,
    ) {
        if (selected.size != choice.choose) {
            validations += blocking(LevelUpValidationCode.ChoiceRequired, "Select ${choice.choose} option(s) for $label.")
            return
        }
        val legal = legalOptionIds ?: optionsFor(choice).mapTo(hashSetOf()) { it.id }
        if ((legalOptionIds != null || legal.isNotEmpty()) && selected.any { it !in legal }) {
            validations += blocking(LevelUpValidationCode.InvalidChoice, "A selected option is not valid for $label.")
        }
    }

    private fun validateFeatChoice(
        id: String,
        choice: Choice.AbilityBonusChoice,
        selected: Set<String>,
        abilities: AbilityScores,
        label: String,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        validateChoice(id, choice, selected, label, validations)
        val selectedEffects = selected.mapNotNull { ability -> choice.from.firstOrNull { ability in it } }
        if (selectedEffects.size != selected.size) return
        val adjusted = selectedEffects.flatMap { it.entries }.fold(abilities) { scores, (ability, amount) ->
            updateAbility(scores, ability, amount)
        }
        if (AbilityIds.standardOrder.any { abilityScore(adjusted, it) > 20 }) {
            validations += blocking(LevelUpValidationCode.InvalidAbilityScoreIncrease, "A feat cannot increase an ability score above 20.")
        }
    }

    private fun optionsFor(choice: Choice): List<LevelUpChoiceOption> = when (choice) {
        is Choice.OptionsArrayChoice -> choice.from.map(::LevelUpChoiceOption)
        is Choice.ProficiencyChoice -> choice.from.map(::LevelUpChoiceOption)
        is Choice.FeatureChoice -> choice.from.map(::LevelUpChoiceOption)
        is Choice.AbilityBonusChoice -> choice.from.flatMap { it.keys }.distinct().sorted().map(::LevelUpChoiceOption)
        is Choice.NestedChoice -> choice.from.flatMap(::optionsFor)
        else -> emptyList()
    }

    private fun spellPolicyId(policy: SpellLearningPolicy): String = when (policy) {
        SpellLearningPolicy.None -> "none"
        is SpellLearningPolicy.Known -> "known"
        is SpellLearningPolicy.Prepared -> "prepared"
        is SpellLearningPolicy.Spellbook -> "spellbook"
    }

    private fun spellRequirementFor(
        clazz: CharacterClass,
        classLevel: Int,
        policy: SpellLearningPolicy,
        subclassId: String?,
        progression: CharacterProgression,
        changes: com.github.arhor.spellbindr.domain.model.SpellChanges,
        preparationCapacity: Int?,
        data: LevelUpReferenceData,
    ): LevelUpRequirement.SpellDecisions {
        val currentSpellcasting = clazz.levels.firstOrNull { it.level == classLevel }?.spellcasting
        val previousSpellcasting = clazz.levels.firstOrNull { it.level == classLevel - 1 }?.spellcasting
        val requiredCantrips = (currentSpellcasting?.cantrips.orZero() - previousSpellcasting?.cantrips.orZero())
            .coerceAtLeast(0)
        val maximumSpellLevel = maximumSpellLevel(currentSpellcasting)
        val ownedSpellIds = classOwnedSpellIds(progression, clazz.id)
        val legalSpells = data.spells.asSequence()
            .filter { spell -> clazz.id in spell.classes.map { it.id } && spell.level <= maximumSpellLevel }
            .sortedWith(compareBy({ it.level }, { it.name }, { it.id }))
            .toList()
        val cantrips = legalSpells.filter { it.level == 0 && it.id !in ownedSpellIds }.map { spell ->
            LevelUpSpellOption(spell.id, spell.name, spell.level)
        }
        val leveledSpells = legalSpells.filter { it.level > 0 && it.id !in ownedSpellIds }.map { spell ->
            LevelUpSpellOption(spell.id, spell.name, spell.level)
        }
        val selectedReplacementSpellId = changes.replaced.singleOrNull()?.learnedSpellId
        val selectedLearnedSpellIds = changes.learned.mapTo(hashSetOf()) { it.spellId }
        val requiredKnown = if (policy is SpellLearningPolicy.Known) {
            val classMagicalSecretCount = magicalSecretFeatures(clazz, classLevel, subclassId, data)
                .count { it.first in BARD_MAGICAL_SECRETS } * 2
            ((currentSpellcasting?.spells.orZero() - previousSpellcasting?.spells.orZero()).coerceAtLeast(0) -
                classMagicalSecretCount).coerceAtLeast(0)
        } else {
            0
        }
        val requiredSpellbook = when (policy) {
            is SpellLearningPolicy.Spellbook -> if (classLevel == 1) {
                policy.spellsAtFirstLevel
            } else {
                policy.spellsAddedPerLevel
            }
            else -> 0
        }
        val replacement = (policy as? SpellLearningPolicy.Known)
            ?.takeIf { classLevel >= it.replacementStartsAtLevel }
            ?.let {
                val selected = changes.replaced.singleOrNull()
                val sourceCandidates = ownedSpellIds.mapNotNull(data.spellsById::get)
                    .filter { spell -> spell.level > 0 }
                    .sortedWith(compareBy({ spell -> spell.level }, { spell -> spell.name }, { spell -> spell.id }))
                    .map { spell -> LevelUpSpellOption(spell.id, spell.name, spell.level) }
                LevelUpSpellReplacementRequirement(
                    sourceCandidates = sourceCandidates,
                    replacementCandidates = leveledSpells.filter { option ->
                        option.spellId !in selectedLearnedSpellIds || option.spellId == selectedReplacementSpellId
                    },
                    selectedSourceSpellId = changes.replacementSourceSpellId ?: selected?.removedSpellId,
                    selectedReplacementSpellId = selected?.learnedSpellId,
                )
            }
        val featureSpellGrants = magicalSecretFeatures(clazz, classLevel, subclassId, data).map { (featureId, label) ->
            val selectedForFeature = changes.featureLearned[featureId].orEmpty().mapTo(hashSetOf()) { it.spellId }
            val selectedElsewhere = changes.learned.mapTo(hashSetOf()) { it.spellId } +
                changes.addedToSpellbook.map { it.spellId } +
                changes.featureLearned.filterKeys { it != featureId }.values.flatten().map { it.spellId } +
                changes.replaced.map { it.learnedSpellId }
            val candidates = data.spells.asSequence()
                .filter { it.level <= maximumSpellLevel }
                .filter { it.id !in ownedSpellIds }
                .filter { it.id !in selectedElsewhere || it.id in selectedForFeature }
                .sortedWith(compareBy({ it.level }, { it.name }, { it.id }))
                .map { LevelUpSpellOption(it.id, it.name, it.level) }
                .toList()
            LevelUpFeatureSpellGrantRequirement(
                featureId = featureId,
                label = label,
                requiredCount = 2,
                candidates = candidates,
                selectedSpellIds = selectedForFeature,
            )
        }
        return LevelUpRequirement.SpellDecisions(
            id = "${clazz.id}:$classLevel:spells",
            classId = clazz.id,
            classLevel = classLevel,
            policyId = spellPolicyId(policy),
            changes = changes,
            requiredCantripCount = requiredCantrips,
            cantripCandidates = cantrips,
            requiredKnownSpellCount = requiredKnown,
            knownSpellCandidates = if (policy is SpellLearningPolicy.Known) {
                leveledSpells.filter { it.spellId != selectedReplacementSpellId }
            } else {
                emptyList()
            },
            featureSpellGrants = featureSpellGrants,
            replacement = replacement,
            requiredSpellbookAdditionCount = requiredSpellbook,
            spellbookCandidates = if (policy is SpellLearningPolicy.Spellbook) leveledSpells else emptyList(),
            preparationCapacity = preparationCapacity,
        )
    }

    private fun magicalSecretFeatures(
        clazz: CharacterClass,
        classLevel: Int,
        subclassId: String?,
        data: LevelUpReferenceData,
    ): List<Pair<String, String>> {
        if (clazz.id != "bard") return emptyList()
        val classFeatureIds = clazz.levels.firstOrNull { it.level == classLevel }?.features.orEmpty()
        val subclassFeatureIds = subclassId?.let { selectedSubclassId ->
            clazz.subclasses.firstOrNull { it.id == selectedSubclassId }?.levels
                ?.firstOrNull { it.level == classLevel }?.features.orEmpty()
        }.orEmpty()
        return (classFeatureIds + subclassFeatureIds)
            .filter { it in BARD_MAGICAL_SECRETS || it == ADDITIONAL_MAGICAL_SECRETS }
            .distinct()
            .map { featureId -> featureId to (data.featuresById[featureId]?.name ?: featureId) }
    }

    private fun maximumSpellLevel(spellcasting: com.github.arhor.spellbindr.domain.model.LevelSpellcasting?): Int =
        spellcasting?.spellSlots.orEmpty().keys.mapNotNull(String::toIntOrNull).maxOrNull().orZero()

    private fun preparationCapacity(
        policy: SpellLearningPolicy,
        classLevel: Int,
        abilities: AbilityScores,
        plan: LevelUpPlan,
        data: LevelUpReferenceData,
    ): Int? {
        val preparation = when (policy) {
            is SpellLearningPolicy.Prepared -> policy.preparation
            is SpellLearningPolicy.Spellbook -> policy.preparation
            else -> null
        } ?: return null
        val afterDecision = applyAbilityDecision(abilities, plan.selections, data)
        return (classLevel / preparation.levelDivisor + afterDecision.modifierFor(preparation.abilityId))
            .coerceAtLeast(preparation.minimumPreparedSpells)
    }

    /**
     * Preparation is mutable sheet state, not progression state. Never normalize it while
     * materializing a level: an ability-score change or multiclass transition can make existing
     * entries illegal, and those entries must be reported so the user can resolve them explicitly.
     */
    private fun validatePreparedSpells(
        sheet: CharacterSheet,
        abilities: AbilityScores,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        data.classes.sortedBy { it.id }.forEach { clazz ->
            val classLevel = progression.classLevels[clazz.id] ?: return@forEach
            val policy = LevelUpReferenceRules.policyFor(clazz.id)?.spells ?: return@forEach
            val preparation = when (policy) {
                is SpellLearningPolicy.Prepared -> policy.preparation
                is SpellLearningPolicy.Spellbook -> policy.preparation
                else -> return@forEach
            }
            val classSpells = sheet.characterSpells.filter { stored ->
                stored.preparation == CharacterSpellPreparation.Prepared &&
                    (stored.sourceClass.equals(clazz.id, ignoreCase = true) ||
                        stored.sourceClass.equals(clazz.name, ignoreCase = true))
            }
            if (classSpells.isEmpty()) return@forEach
            val maxSpellLevel = clazz.levels.firstOrNull { it.level == classLevel }
                ?.spellcasting?.spellSlots.orEmpty().keys.mapNotNull(String::toIntOrNull).maxOrNull().orZero()
            val capacity = (classLevel / preparation.levelDivisor + abilities.modifierFor(preparation.abilityId))
                .coerceAtLeast(preparation.minimumPreparedSpells)
            val illegal = classSpells.filter { stored ->
                val spell = data.spellsById[stored.spellId]
                spell == null || spell.level == 0 || spell.level > maxSpellLevel ||
                    clazz.id !in spell.classes.map { it.id }
            }
            val preparedLeveled = classSpells.filter { stored ->
                val spell = data.spellsById[stored.spellId]
                spell != null && spell.level > 0 && stored !in illegal
            }
            val overflow = preparedLeveled.drop(capacity)
            (illegal + overflow).map { it.spellId }.distinct().sorted().forEach { spellId ->
                validations += blocking(
                    LevelUpValidationCode.SpellPolicy,
                    "Prepared spell ${clazz.id}:$spellId is no longer legal for ${clazz.name}; choose a replacement.",
                )
            }
        }
    }

    private fun meetsPrerequisites(abilities: AbilityScores, prerequisites: List<AbilityScorePrerequisite>): Boolean =
        prerequisites.all { prerequisite ->
            val checks = prerequisite.abilityScore.map { abilityScore(abilities, it) >= prerequisite.minimumScore }
            if (prerequisite.atLeastOne) checks.any { it } else checks.all { it }
        }

    private fun classEligibility(
        abilities: AbilityScores,
        progression: CharacterProgression,
        candidate: CharacterClass,
        data: LevelUpReferenceData,
    ): LevelUpClassEligibility {
        val reasons = buildList {
            if (progression.classLevels[candidate.id].orZero() >= LevelUpReferenceRules.maximumCharacterLevel) {
                add("Maximum class level reached.")
            }
            if (candidate.id !in progression.classLevels) {
                (progression.classLevels.keys + candidate.id).sorted().forEach { classId ->
                    val clazz = data.classesById[classId]
                    val rules = clazz?.multiClassing
                    when {
                        rules == null -> add("Multiclass rules are missing for ${clazz?.name ?: classId}.")
                        !meetsPrerequisites(abilities, rules.prerequisites) ->
                            add("Ability prerequisites for ${clazz.name} are not met; confirmation requires an override.")
                    }
                }
            }
        }
        return LevelUpClassEligibility(candidate.id, reasons.isEmpty(), reasons)
    }

    /** Reconstructs the class-owned learned/spellbook state in level order. */
    internal fun classOwnedSpellIds(progression: CharacterProgression, classId: String): Set<String> =
        progression.levels.asSequence().filter { it.classId == classId }.fold(linkedSetOf()) { known, record ->
            record.spellChanges.replaced.forEach { replacement ->
                known.remove(replacement.removedSpellId)
                known.add(replacement.learnedSpellId)
            }
            known.addAll(record.spellChanges.learned.map { it.spellId })
            known.addAll(record.spellChanges.addedToSpellbook.map { it.spellId })
            known.addAll(
                record.spellChanges.featureLearned.values.flatten()
                    .filter { it.classId == classId }
                    .map { it.spellId },
            )
            known
        }

    private fun meetsFeatPrerequisites(
        abilities: AbilityScores,
        proficiencyIds: Set<String>,
        hasKnownSpell: Boolean,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
        prerequisites: List<Prerequisite>,
    ): Boolean = prerequisites.all { prerequisite -> when (prerequisite) {
        is Prerequisite.AbilityScorePrerequisite -> {
            val checks = prerequisite.abilityScore.map { abilityScore(abilities, it) >= prerequisite.minimumValue }
            if (prerequisite.atLeastOne) checks.any { it } else checks.all { it }
        }
        is Prerequisite.ProficiencyPrerequisite -> prerequisite.id in proficiencyIds
        Prerequisite.SpellcastingPrerequisite -> hasSpellcasting(hasKnownSpell, progression, data)
        else -> true
    } }

    private fun hasSpellcasting(
        hasKnownSpell: Boolean,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): Boolean = hasKnownSpell || progression.classLevels.any { (classId, classLevel) ->
            val hasLevelSpellcasting = data.classesById[classId]?.levels
                ?.firstOrNull { it.level == classLevel }?.spellcasting != null
            hasLevelSpellcasting || when (casterContributionFor(classId, progression)) {
                CasterContribution.Full, CasterContribution.Pact -> classLevel >= 1
                CasterContribution.Half -> classLevel >= 2
                CasterContribution.Third -> classLevel >= 3
                CasterContribution.None -> false
            }
        }

    private fun featEligibilityFor(
        feat: Feat,
        abilities: AbilityScores,
        maximum: Int,
        proficiencyIds: Set<String>,
        languageIds: Set<String>,
        hasKnownSpell: Boolean,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): LevelUpFeatEligibility {
        val deferredDecision = deferredFeatDecision(feat.id)
            ?.takeUnless {
                (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(data)) ||
                    (feat.id == SPELL_SNIPER_ID && spellSniperIsSupported(data))
            }
            ?.takeUnless { feat.id == MARTIAL_ADEPT_ID && feat.maneuverChoice != null }
        val reasons = buildList {
            if (!feat.repeatable && progression.levels.any { record ->
                    (record.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId == feat.id
                }
            ) add("This feat cannot be selected more than once.")
            deferredDecision?.let { add(deferredFeatReason(it)) }
            if (!meetsFeatPrerequisites(
                    abilities,
                    proficiencyIds,
                    hasKnownSpell,
                    progression,
                    data,
                    feat.prerequisites,
                )
            ) {
                add("Current character prerequisites are not met.")
            }
            if (!featHasLegalOwnedChoices(
                    feat,
                    abilities,
                    maximum,
                    proficiencyIds,
                    languageIds,
                    progression,
                    data,
                )
            ) add("No legal combination remains for this feat's required choices.")
        }
        return LevelUpFeatEligibility(feat.id, reasons.isEmpty(), reasons, deferredDecision)
    }

    private fun deferredFeatDecision(featId: String): LevelUpDeferredFeatDecision? = when (featId) {
        "magic-initiate", "ritual-caster", "spell-sniper" -> LevelUpDeferredFeatDecision.SpellSelection
        "martial-adept" -> LevelUpDeferredFeatDecision.ManeuverSelection
        else -> null
    }

    private fun deferredFeatReason(decision: LevelUpDeferredFeatDecision): String = when (decision) {
        LevelUpDeferredFeatDecision.SpellSelection ->
            "This feat's spell ownership and casting rules cannot be represented by the bundled progression model."
        LevelUpDeferredFeatDecision.ManeuverSelection ->
            "This feat requires maneuver choices that are not available in the bundled reference model."
    }

    private fun featHasLegalOwnedChoices(
        feat: Feat,
        abilities: AbilityScores,
        maximum: Int,
        proficiencyIds: Set<String>,
        languageIds: Set<String>,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): Boolean {
        val afterFixed = applyFixedFeatAbilityEffects(abilities, feat)
        if (AbilityIds.standardOrder.any { abilityScore(afterFixed, it) > maximum }) return false
        val abilityChoiceIsLegal = if (feat.correlatesAbilityAndSavingThrow) {
            legalCorrelatedAbilities(feat, abilities, proficiencyIds, maximum).isNotEmpty()
        } else feat.abilityBonusChoice?.let { choice ->
            legalFeatAbilityOptions(feat, abilities, maximum).size >= choice.choose
        } ?: true
        val languageChoiceIsLegal = feat.languageChoice?.choose?.let {
            it <= data.languages.count { language -> language.id !in languageIds }
        } ?: true
        val proficiencyChoiceIsLegal = if (feat.correlatesAbilityAndSavingThrow) true else feat.proficiencyChoice?.let { choice ->
            choice.choose <= choice.from.distinct().count { it !in proficiencyIds }
        } ?: true
        val damageTypeChoiceIsLegal = feat.damageTypeChoice?.let { choice ->
            choice.choose <= legalDamageTypes(feat, progression).size
        } ?: true
        val maneuverChoiceIsLegal = feat.maneuverChoice?.let { choice ->
            choice.choose <= choice.from.distinct().size
        } ?: true
        val magicInitiateChoicesAreLegal = feat.id != MAGIC_INITIATE_ID || magicInitiateIsSupported(data)
        val spellSniperChoicesAreLegal = feat.id != SPELL_SNIPER_ID || spellSniperIsSupported(data)
        return abilityChoiceIsLegal && languageChoiceIsLegal && proficiencyChoiceIsLegal && damageTypeChoiceIsLegal &&
            maneuverChoiceIsLegal &&
            magicInitiateChoicesAreLegal && spellSniperChoicesAreLegal
    }

    private fun legalFeatAbilityOptions(
        feat: Feat,
        abilities: AbilityScores,
        maximum: Int,
    ): List<Map<String, Int>> {
        val afterFixed = applyFixedFeatAbilityEffects(abilities, feat)
        return feat.abilityBonusChoice?.from.orEmpty().filter { option ->
            option.all { (ability, amount) -> abilityScore(afterFixed, ability) + amount <= maximum }
        }
    }

    private fun applyFixedFeatAbilityEffects(
        abilities: AbilityScores,
        feat: Feat,
    ): AbilityScores = feat.effects.filterIsInstance<Effect.ModifyAbilityEffect>()
        .flatMap { it.abilities.entries }
        .fold(abilities) { scores, (ability, amount) -> updateAbility(scores, ability, amount) }

    private fun activeFeatChoiceIds(feat: Feat): Set<String> = feat.ownedChoiceIds + when (feat.id) {
        MAGIC_INITIATE_ID -> MAGIC_INITIATE_CHOICE_IDS
        SPELL_SNIPER_ID -> SPELL_SNIPER_CHOICE_IDS
        else -> emptySet()
    }

    private fun magicInitiateIsSupported(data: LevelUpReferenceData): Boolean =
        magicInitiateClassIds(data).isNotEmpty()

    private fun magicInitiateClassIds(data: LevelUpReferenceData): List<String> = MAGIC_INITIATE_CLASS_IDS
        .filter { classId ->
            classId in data.classesById &&
                magicInitiateSpellCandidates(data, classId, 0).size >= 2 &&
                magicInitiateSpellCandidates(data, classId, 1).isNotEmpty()
        }
        .sorted()

    private fun magicInitiateSpellCandidates(
        data: LevelUpReferenceData,
        classId: String,
        level: Int,
    ) = data.spells.asSequence()
        .filter { spell -> spell.level == level && classId in spell.classes.map { it.id } }
        .sortedWith(compareBy({ it.name }, { it.id }))
        .toList()

    private fun validateMagicInitiateSelections(
        selections: LevelUpSelections,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val classIds = magicInitiateClassIds(data)
        validateChoice(
            id = MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, classIds),
            selected = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID].orEmpty(),
            label = "Magic Initiate spell list",
            validations = validations,
            legalOptionIds = classIds.toSet(),
        )
        val classId = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID]
            ?.singleOrNull()
            ?.takeIf { it in classIds }
            ?: return
        val cantripIds = magicInitiateSpellCandidates(data, classId, 0).map { it.id }
        validateChoice(
            id = MAGIC_INITIATE_CANTRIP_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(2, cantripIds),
            selected = selections.featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty(),
            label = "Magic Initiate cantrips",
            validations = validations,
            legalOptionIds = cantripIds.toSet(),
        )
        val firstLevelIds = magicInitiateSpellCandidates(data, classId, 1).map { it.id }
        validateChoice(
            id = MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, firstLevelIds),
            selected = selections.featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty(),
            label = "Magic Initiate 1st-level spell",
            validations = validations,
            legalOptionIds = firstLevelIds.toSet(),
        )
    }

    private fun magicInitiateChoiceRequirements(
        data: LevelUpReferenceData,
        featChoices: Map<String, Set<String>>,
    ): List<LevelUpRequirement.ChoiceSelection> = buildList {
        val classIds = magicInitiateClassIds(data)
        val classSelection = featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID].orEmpty()
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate spell list",
            choice = Choice.OptionsArrayChoice(1, classIds),
            selectedOptionIds = classSelection,
            category = LevelUpChoiceCategory.Feat,
            options = classIds.map { classId ->
                LevelUpChoiceOption(classId, data.classesById[classId]?.name ?: classId)
            },
        ))
        val classId = classSelection.singleOrNull()?.takeIf { it in classIds } ?: return@buildList
        val cantrips = magicInitiateSpellCandidates(data, classId, 0)
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_CANTRIP_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate cantrips",
            choice = Choice.OptionsArrayChoice(2, cantrips.map { it.id }),
            selectedOptionIds = featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty(),
            category = LevelUpChoiceCategory.Feat,
            options = cantrips.map { LevelUpChoiceOption(it.id, it.name) },
        ))
        val firstLevelSpells = magicInitiateSpellCandidates(data, classId, 1)
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate 1st-level spell",
            choice = Choice.OptionsArrayChoice(1, firstLevelSpells.map { it.id }),
            selectedOptionIds = featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty(),
            category = LevelUpChoiceCategory.Feat,
            options = firstLevelSpells.map { LevelUpChoiceOption(it.id, it.name) },
        ))
    }

    private fun LevelUpPlan.withMagicInitiateSpellGrant(): LevelUpPlan {
        val classId = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID]?.singleOrNull() ?: return this
        val spellIds = selections.featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty() +
            selections.featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty()
        val grants = spellIds.mapTo(linkedSetOf()) { spellId ->
            com.github.arhor.spellbindr.domain.model.ClassSpellRef(classId, spellId)
        }
        return copy(selections = selections.copy(
            spellChanges = selections.spellChanges.copy(
                featureLearned = selections.spellChanges.featureLearned +
                    (MAGIC_INITIATE_SPELL_GRANT_OWNER_ID to grants),
            ),
        ))
    }

    private fun spellSniperSpellCandidates(data: LevelUpReferenceData, classId: String): List<Spell> =
        data.spells.asSequence()
            .filter { spell -> spell.level == 0 && classId in spell.classes.map { it.id } && spell.attackType != null }
            .sortedWith(compareBy({ it.name }, { it.id }))
            .toList()

    private fun spellSniperClassIds(data: LevelUpReferenceData): List<String> =
        SPELL_SNIPER_CLASS_IDS.filter { spellSniperSpellCandidates(data, it).isNotEmpty() }.sorted()

    private fun spellSniperIsSupported(data: LevelUpReferenceData): Boolean =
        spellSniperClassIds(data).isNotEmpty()

    private fun validateSpellSniperSelections(
        selections: LevelUpSelections,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val classIds = spellSniperClassIds(data)
        validateChoice(
            id = SPELL_SNIPER_CLASS_LIST_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, classIds),
            selected = selections.featChoices[SPELL_SNIPER_CLASS_LIST_CHOICE_ID].orEmpty(),
            label = "Spell Sniper spell list",
            validations = validations,
            legalOptionIds = classIds.toSet(),
        )
        val classId = selections.featChoices[SPELL_SNIPER_CLASS_LIST_CHOICE_ID]
            ?.singleOrNull()?.takeIf { it in classIds } ?: return
        val cantripIds = spellSniperSpellCandidates(data, classId).map { it.id }
        validateChoice(
            id = SPELL_SNIPER_CANTRIP_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, cantripIds),
            selected = selections.featChoices[SPELL_SNIPER_CANTRIP_CHOICE_ID].orEmpty(),
            label = "Spell Sniper attack-roll cantrip",
            validations = validations,
            legalOptionIds = cantripIds.toSet(),
        )
    }

    private fun spellSniperChoiceRequirements(
        data: LevelUpReferenceData,
        featChoices: Map<String, Set<String>>,
    ): List<LevelUpRequirement.ChoiceSelection> = buildList {
        val classIds = spellSniperClassIds(data)
        val selectedClass = featChoices[SPELL_SNIPER_CLASS_LIST_CHOICE_ID].orEmpty()
        add(LevelUpRequirement.ChoiceSelection(
            id = SPELL_SNIPER_CLASS_LIST_CHOICE_ID,
            sourceId = SPELL_SNIPER_ID,
            label = "Spell Sniper spell list",
            choice = Choice.OptionsArrayChoice(1, classIds),
            selectedOptionIds = selectedClass,
            category = LevelUpChoiceCategory.Feat,
            options = classIds.map { LevelUpChoiceOption(it, data.classesById[it]?.name ?: it) },
        ))
        val classId = selectedClass.singleOrNull()?.takeIf { it in classIds } ?: return@buildList
        val cantrips = spellSniperSpellCandidates(data, classId)
        add(LevelUpRequirement.ChoiceSelection(
            id = SPELL_SNIPER_CANTRIP_CHOICE_ID,
            sourceId = SPELL_SNIPER_ID,
            label = "Spell Sniper attack-roll cantrip",
            choice = Choice.OptionsArrayChoice(1, cantrips.map { it.id }),
            selectedOptionIds = featChoices[SPELL_SNIPER_CANTRIP_CHOICE_ID].orEmpty(),
            category = LevelUpChoiceCategory.Feat,
            options = cantrips.map { LevelUpChoiceOption(it.id, it.name) },
        ))
    }

    private fun LevelUpPlan.withSpellSniperSpellGrant(): LevelUpPlan {
        val classId = selections.featChoices[SPELL_SNIPER_CLASS_LIST_CHOICE_ID]?.singleOrNull() ?: return this
        val spellId = selections.featChoices[SPELL_SNIPER_CANTRIP_CHOICE_ID]?.singleOrNull() ?: return this
        return copy(selections = selections.copy(
            spellChanges = selections.spellChanges.copy(
                featureLearned = selections.spellChanges.featureLearned +
                    (SPELL_SNIPER_SPELL_GRANT_OWNER_ID to setOf(ClassSpellRef(classId, spellId))),
            ),
        ))
    }

    private data class FeatOwnedChoiceRequirement(
        val id: String,
        val choice: Choice,
        val options: List<LevelUpChoiceOption>,
    )

    private fun featOwnedChoiceRequirements(
        feat: Feat,
        abilities: AbilityScores,
        maximum: Int,
        proficiencyIds: Set<String>,
        languageIds: Set<String>,
        progression: CharacterProgression,
        data: LevelUpReferenceData,
    ): List<FeatOwnedChoiceRequirement> = buildList {
        if (feat.correlatesAbilityAndSavingThrow) {
            val legal = legalCorrelatedAbilities(feat, abilities, proficiencyIds, maximum)
            add(FeatOwnedChoiceRequirement(
                id = feat.correlatedAbilitySavingThrowChoiceId.orEmpty(),
                choice = Choice.OptionsArrayChoice(choose = 1, from = legal),
                options = legal.map { LevelUpChoiceOption(it, "+1 ${it.uppercase()} and saving throw") },
            ))
        } else feat.abilityBonusChoice?.let { choice ->
            val legal = legalFeatAbilityOptions(feat, abilities, maximum)
            add(FeatOwnedChoiceRequirement(
                id = feat.abilityBonusChoiceId.orEmpty(),
                choice = choice.copy(from = legal),
                options = legal.flatMap { option ->
                    option.map { (ability, increase) ->
                        LevelUpChoiceOption(ability, "+$increase ${ability.uppercase()}")
                    }
                },
            ))
        }
        feat.languageChoice?.let { choice ->
            add(FeatOwnedChoiceRequirement(
                id = feat.languageChoiceId.orEmpty(),
                choice = choice,
                options = data.languages.filter { it.id !in languageIds }.sortedBy { it.id }
                    .map { LevelUpChoiceOption(it.id, it.name) },
            ))
        }
        if (!feat.correlatesAbilityAndSavingThrow) feat.proficiencyChoice?.let { choice ->
            add(FeatOwnedChoiceRequirement(
                id = feat.proficiencyChoiceId.orEmpty(),
                choice = choice,
                options = choice.from.filter { it !in proficiencyIds }.distinct().sorted().map(::LevelUpChoiceOption),
            ))
        }
        feat.damageTypeChoice?.let { choice ->
            val legal = legalDamageTypes(feat, progression).sorted()
            add(FeatOwnedChoiceRequirement(
                id = feat.damageTypeChoiceId.orEmpty(),
                choice = choice.copy(from = legal),
                options = legal.map { LevelUpChoiceOption(it, it.replaceFirstChar { char -> char.uppercase() }) },
            ))
        }
    }

    private fun legalCorrelatedAbilities(
        feat: Feat,
        abilities: AbilityScores,
        proficiencyIds: Set<String>,
        maximum: Int,
    ): List<String> {
        val abilityIds = feat.abilityBonusChoice?.from.orEmpty().flatMap { it.keys }.toSet()
        val savingThrowIds = feat.proficiencyChoice?.from.orEmpty()
            .mapNotNull { it.removePrefix(SAVING_THROW_PREFIX).takeIf { ability ->
                it.startsWith(SAVING_THROW_PREFIX) && ability in AbilityIds.standardOrder
            } }
            .toSet()
        return AbilityIds.standardOrder.filter { ability ->
            ability in abilityIds && ability in savingThrowIds &&
                abilityScore(abilities, ability) < maximum && "$SAVING_THROW_PREFIX$ability" !in proficiencyIds
        }
    }

    private fun validateCorrelatedAbilitySavingThrowChoice(
        feat: Feat,
        selections: LevelUpSelections,
        abilities: AbilityScores,
        proficiencyIds: Set<String>,
        maximum: Int,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val legacyAbility = feat.abilityBonusChoiceId?.let { selections.featChoices[it] }.orEmpty()
        val legacySavingThrow = feat.proficiencyChoiceId?.let { selections.featChoices[it] }.orEmpty()
        if (legacyAbility.isNotEmpty() || legacySavingThrow.isNotEmpty()) {
            validations += blocking(
                LevelUpValidationCode.InvalidChoice,
                "${feat.name} must use one correlated ability and saving throw selection.",
            )
        }
        val choiceId = feat.correlatedAbilitySavingThrowChoiceId.orEmpty()
        val legal = legalCorrelatedAbilities(feat, abilities, proficiencyIds, maximum)
        validateChoice(
            id = choiceId,
            choice = Choice.OptionsArrayChoice(1, legal),
            selected = selections.featChoices[choiceId].orEmpty(),
            label = feat.name,
            validations = validations,
            legalOptionIds = legal.toSet(),
        )
    }

    private fun legalDamageTypes(feat: Feat, progression: CharacterProgression): Set<String> {
        val prior = progression.levels.asSequence()
            .filter { (it.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId == feat.id }
            .flatMap { record -> feat.damageTypeChoiceId?.let { record.featChoices[it].orEmpty() }.orEmpty().asSequence() }
            .toSet()
        return feat.damageTypeChoice?.from.orEmpty().toSet() - prior
    }

    private fun applyAbilityDecision(
        base: AbilityScores,
        selections: LevelUpSelections,
        data: LevelUpReferenceData,
    ): AbilityScores = when (val decision = selections.abilityScoreDecision) {
        is AbilityScoreDecision.Increase -> decision.increases.entries.fold(base) { scores, (ability, amount) ->
            updateAbility(scores, ability, amount)
        }
        is AbilityScoreDecision.Feat -> {
            data.featsById[decision.featId]?.let { feat ->
                val fixed = feat.effects.filterIsInstance<Effect.ModifyAbilityEffect>().flatMap { it.abilities.entries }
                    .fold(base) { scores, (ability, amount) -> updateAbility(scores, ability, amount) }
                val choiceId = feat.correlatedAbilitySavingThrowChoiceId ?: feat.abilityBonusChoiceId
                val choice = feat.abilityBonusChoice
                if (choiceId == null || choice == null) {
                    fixed
                } else {
                    selections.featChoices[choiceId].orEmpty().fold(fixed) { scores, ability ->
                        choice.from.firstOrNull { ability in it }.orEmpty().entries.fold(scores) { current, (id, amount) ->
                            updateAbility(current, id, amount)
                        }
                    }
                }
            } ?: base
        }
        null -> base
    }

    private fun updateAbility(scores: AbilityScores, ability: String, amount: Int): AbilityScores = when (ability) {
        AbilityIds.STR -> scores.copy(strength = scores.strength + amount)
        AbilityIds.DEX -> scores.copy(dexterity = scores.dexterity + amount)
        AbilityIds.CON -> scores.copy(constitution = scores.constitution + amount)
        AbilityIds.INT -> scores.copy(intelligence = scores.intelligence + amount)
        AbilityIds.WIS -> scores.copy(wisdom = scores.wisdom + amount)
        AbilityIds.CHA -> scores.copy(charisma = scores.charisma + amount)
        else -> scores
    }

    private fun abilityScore(scores: AbilityScores, ability: String): Int = when (ability.lowercase()) {
        AbilityIds.STR -> scores.strength
        AbilityIds.DEX -> scores.dexterity
        AbilityIds.CON -> scores.constitution
        AbilityIds.INT -> scores.intelligence
        AbilityIds.WIS -> scores.wisdom
        AbilityIds.CHA -> scores.charisma
        else -> Int.MIN_VALUE
    }

    private fun subclassFor(classId: String, progression: CharacterProgression): String? = progression.levels
        .asReversed().firstOrNull { it.classId == classId && it.subclassId != null }?.subclassId

    private fun casterContributionFor(classId: String, progression: CharacterProgression): CasterContribution {
        val policy = LevelUpReferenceRules.policyFor(classId) ?: return CasterContribution.None
        return policy.subclassCasterContributions[subclassFor(classId, progression)] ?: policy.casterContribution
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun manualLanguageIds(rawLanguages: String, data: LevelUpReferenceData): Set<String> {
        val entries = rawLanguages.split(',', ';', '\n', '/', '|').map { it.trim().lowercase() }.filter(String::isNotEmpty)
        return data.languages.asSequence()
            .filter { language -> language.id.lowercase() in entries || language.name.lowercase() in entries }
            .mapTo(linkedSetOf()) { it.id }
    }

    private const val MAGIC_INITIATE_ID = "magic-initiate"
    private const val MARTIAL_ADEPT_ID = "martial-adept"
    private const val SUPERIORITY_DIE_RESOURCE_ID = "superiority-die"
    private const val MAGIC_INITIATE_CLASS_LIST_CHOICE_ID = "magic-initiate:class-list"
    private const val MAGIC_INITIATE_CANTRIP_CHOICE_ID = "magic-initiate:cantrips"
    private const val MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID = "magic-initiate:first-level-spell"
    private const val MAGIC_INITIATE_SPELL_GRANT_OWNER_ID = "feat:magic-initiate"
    private const val SPELL_SNIPER_ID = "spell-sniper"
    private const val SPELL_SNIPER_CLASS_LIST_CHOICE_ID = "spell-sniper:class-list"
    private const val SPELL_SNIPER_CANTRIP_CHOICE_ID = "spell-sniper:cantrip"
    private const val SPELL_SNIPER_SPELL_GRANT_OWNER_ID = "feat:spell-sniper"
    private val MAGIC_INITIATE_CLASS_IDS = setOf("bard", "cleric", "druid", "sorcerer", "warlock", "wizard")
    private val MAGIC_INITIATE_CHOICE_IDS = setOf(
        MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
        MAGIC_INITIATE_CANTRIP_CHOICE_ID,
        MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
    )
    private val SPELL_SNIPER_CLASS_IDS = setOf("bard", "cleric", "druid", "sorcerer", "warlock", "wizard")
    private val SPELL_SNIPER_CHOICE_IDS = setOf(SPELL_SNIPER_CLASS_LIST_CHOICE_ID, SPELL_SNIPER_CANTRIP_CHOICE_ID)

    private const val SAVING_THROW_PREFIX = "saving-throw-"
    private const val ADDITIONAL_MAGICAL_SECRETS = "additional-magical-secrets"
    private val BARD_MAGICAL_SECRETS = setOf("magical-secrets-1", "magical-secrets-2", "magical-secrets-3")

    private fun blocking(code: LevelUpValidationCode, message: String) =
        LevelUpValidationIssue(code, message, LevelUpValidationSeverity.Blocking)

    private fun overrideable(
        code: LevelUpValidationCode,
        message: String,
        findingId: String? = null,
    ) = LevelUpValidationIssue(code, message, LevelUpValidationSeverity.Overrideable, findingId)
}
