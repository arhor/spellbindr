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
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpClassEligibility
import com.github.arhor.spellbindr.domain.model.LevelUpHitDicePool
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.ProficiencyChoiceSelection
import com.github.arhor.spellbindr.domain.model.SpellLearningPolicy
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
 * Spell legality deliberately remains a contract-only requirement here; the spell subsystem can extend it without
 * changing level ordering, multiclass checks, or the persisted draft shape.
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
        return LevelUpPreview(before, after, requirements, validations.distinctBy { it.code to it.message })
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
        val featIds = (plan.selections.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId
            ?.let(referenceData.featsById::get)?.abilityBonusChoiceId?.let(::setOf).orEmpty()
        return plan.toRecord(
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
        validateHitPointGain(selections.hitPointGain, clazz.hitDie, nextClassLevel, validations)
        val asiPolicy = LevelUpReferenceRules.policyFor(clazz.id)?.abilityScoreImprovement
        if (asiPolicy?.levels?.contains(nextClassLevel) == true) {
            validateAbilityDecision(
                sheet.abilityScores,
                snapshot(sheet.abilityScores, progression, data).proficiencyIds,
                selections,
                asiPolicy.abilityPoints,
                asiPolicy.maximumAbilityScore,
                asiPolicy.allowsFeat,
                data,
                validations,
            )
        }
        validateSpellChanges(progression, selections.spellChanges, clazz, nextClassLevel, data, validations)
    }

    private fun validateHitPointGain(
        gain: HitPointGain?,
        hitDie: Int,
        classLevel: Int,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        if (gain == null) {
            validations += blocking(LevelUpValidationCode.HitPointGainRequired, "Choose a hit point gain.")
            return
        }
        val expectedFixed = if (classLevel == 1) hitDie else hitDie / 2 + 1
        val isValid = when (gain) {
            is HitPointGain.Fixed -> gain.rolledValue == expectedFixed
            is HitPointGain.Rolled -> gain.rolledValue in 1..hitDie
            is HitPointGain.Manual -> gain.rolledValue >= 1
        }
        if (!isValid) validations += blocking(LevelUpValidationCode.InvalidHitPointGain, "The hit point gain is invalid.")
    }

    private fun validateAbilityDecision(
        abilities: AbilityScores,
        proficiencyIds: Set<String>,
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
                    if (!meetsFeatPrerequisites(abilities, proficiencyIds, feat.prerequisites)) {
                        validations += blocking(LevelUpValidationCode.FeatPrerequisite, "Prerequisites for ${feat.name} are not met.")
                    }
                    feat.abilityBonusChoice?.let { choice ->
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
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val policy = LevelUpReferenceRules.policyFor(clazz.id)?.spells ?: return
        if (policy == SpellLearningPolicy.None) return
        val allRefs = changes.learned + changes.addedToSpellbook + changes.replaced.flatMapTo(linkedSetOf()) {
            setOf(com.github.arhor.spellbindr.domain.model.ClassSpellRef(it.classId, it.removedSpellId),
                com.github.arhor.spellbindr.domain.model.ClassSpellRef(it.classId, it.learnedSpellId))
        }
        if (allRefs.any { it.classId != clazz.id }) {
            validations += blocking(LevelUpValidationCode.SpellPolicy, "Spell decisions must belong to ${clazz.name}.")
            return
        }
        val classSpellcasting = clazz.levels.firstOrNull { it.level == classLevel }?.spellcasting
        val previousSpellcasting = clazz.levels.firstOrNull { it.level == classLevel - 1 }?.spellcasting
        fun spell(ref: com.github.arhor.spellbindr.domain.model.ClassSpellRef) = data.spellsById[ref.spellId]
        val invalid = allRefs.any { ref ->
            val value = spell(ref)
            value == null || clazz.id !in value.classes.map { it.id } ||
                value.level > classSpellcasting?.spellSlots.orEmpty().keys.maxOfOrNull { it.toInt() }.orZero()
        }
        if (invalid) validations += blocking(LevelUpValidationCode.SpellPolicy, "A selected spell is not legal for this class level.")
        val selectedCantrips = changes.learned.count { spell(it)?.level == 0 }
        val selectedKnown = changes.learned.count { spell(it)?.level?.let { level -> level > 0 } == true }
        val expectedCantrips = (classSpellcasting?.cantrips.orZero() - previousSpellcasting?.cantrips.orZero()).coerceAtLeast(0)
        when (policy) {
            is SpellLearningPolicy.Known -> {
                val expectedKnown = (classSpellcasting?.spells.orZero() - previousSpellcasting?.spells.orZero()).coerceAtLeast(0)
                if (selectedCantrips != expectedCantrips || selectedKnown != expectedKnown) {
                    validations += blocking(LevelUpValidationCode.SpellPolicy, "Choose the required known spells and cantrips for this class level.")
                }
                if (changes.addedToSpellbook.isNotEmpty() || changes.replaced.size > 1 ||
                    (changes.replaced.isNotEmpty() && classLevel < policy.replacementStartsAtLevel)
                ) validations += blocking(LevelUpValidationCode.SpellPolicy, "The selected spell replacement is not allowed at this class level.")
                val currentlyKnown = classOwnedSpellIds(progression, clazz.id)
                val removed = changes.replaced.map { it.removedSpellId }
                val replacements = changes.replaced.map { it.learnedSpellId }
                val learned = changes.learned.map { it.spellId }
                if (removed.distinct().size != removed.size || replacements.distinct().size != replacements.size ||
                    removed.any { it !in currentlyKnown } || changes.replaced.any { it.removedSpellId == it.learnedSpellId } ||
                    replacements.any { it in currentlyKnown && it !in removed } ||
                    (replacements + learned).distinct().size != replacements.size + learned.size ||
                    learned.any { it in currentlyKnown && it !in removed }
                ) {
                    validations += blocking(
                        LevelUpValidationCode.SpellPolicy,
                        "Replacements must remove a currently known class spell and learn a distinct, non-duplicate spell.",
                    )
                }
            }
            is SpellLearningPolicy.Prepared -> {
                if (selectedCantrips != expectedCantrips || selectedKnown != 0 || changes.addedToSpellbook.isNotEmpty() || changes.replaced.isNotEmpty()) {
                    validations += blocking(LevelUpValidationCode.SpellPolicy, "Prepared casters only choose newly gained cantrips here.")
                }
            }
            is SpellLearningPolicy.Spellbook -> {
                val expectedBook = if (classLevel == 1) policy.spellsAtFirstLevel else policy.spellsAddedPerLevel
                if (selectedCantrips != expectedCantrips || selectedKnown != 0 || changes.addedToSpellbook.size != expectedBook || changes.replaced.isNotEmpty()) {
                    validations += blocking(LevelUpValidationCode.SpellPolicy, "Choose the required spellbook additions and cantrips for this class level.")
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
        add(LevelUpRequirement.HitPoints(hitDie = selectedClass.hitDie, selectedGain = plan.selections.hitPointGain))
        LevelUpReferenceRules.policyFor(selectedClass.id)?.let { policy ->
            if (nextClassLevel in policy.abilityScoreImprovement.levels) add(LevelUpRequirement.AbilityScoreImprovement(
                id = "${selectedClass.id}:$nextClassLevel:asi", classId = selectedClass.id,
                abilityPoints = policy.abilityScoreImprovement.abilityPoints,
                maximumAbilityScore = policy.abilityScoreImprovement.maximumAbilityScore,
                allowsFeat = policy.abilityScoreImprovement.allowsFeat,
                selectedDecision = plan.selections.abilityScoreDecision,
            ))
            if (policy.spells != SpellLearningPolicy.None) add(LevelUpRequirement.SpellDecisions(
                id = "${selectedClass.id}:$nextClassLevel:spells", classId = selectedClass.id,
                classLevel = nextClassLevel, policyId = spellPolicyId(policy.spells), changes = plan.selections.spellChanges,
                preparationCapacity = preparationCapacity(policy.spells, nextClassLevel, abilities, plan, referenceData),
            ))
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
        progression.levels.forEachIndexed { index, record ->
            val clazz = classesById[record.classId] ?: return@forEachIndexed
            if (index == 0) {
                proficiencyIds += clazz.proficiencies
                savingThrows += clazz.savingThrows
            } else if (record.classLevel == 1) {
                proficiencyIds += clazz.multiClassing?.proficiencyGrants.orEmpty()
            }
            proficiencyIds += record.proficiencyChoices.flatMap { it.selectedProficiencyIds }
            clazz.levels.firstOrNull { it.level == record.classLevel }?.features?.let(featureIds::addAll)
            record.subclassId?.let { subclassId -> clazz.subclasses.firstOrNull { it.id == subclassId }
                ?.levels?.firstOrNull { it.level == record.classLevel }?.features?.let(featureIds::addAll) }
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
        val maxHp = (progression.levels.sumOf { it.hitPointGain.rolledValue } +
            progression.totalLevel * abilities.modifierFor(AbilityIds.CON)).coerceAtLeast(1)
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
    ) {
        if (selected.size != choice.choose) {
            validations += blocking(LevelUpValidationCode.ChoiceRequired, "Select ${choice.choose} option(s) for $label.")
            return
        }
        val legal = optionsFor(choice).mapTo(hashSetOf()) { it.id }
        if (legal.isNotEmpty() && selected.any { it !in legal }) {
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
            known
        }

    private fun meetsFeatPrerequisites(
        abilities: AbilityScores,
        proficiencyIds: Set<String>,
        prerequisites: List<com.github.arhor.spellbindr.domain.model.Prerequisite>,
    ): Boolean = prerequisites.all { prerequisite -> when (prerequisite) {
        is com.github.arhor.spellbindr.domain.model.Prerequisite.AbilityScorePrerequisite -> {
            val checks = prerequisite.abilityScore.map { abilityScore(abilities, it) >= prerequisite.minimumValue }
            if (prerequisite.atLeastOne) checks.any { it } else checks.all { it }
        }
        is com.github.arhor.spellbindr.domain.model.Prerequisite.ProficiencyPrerequisite -> prerequisite.id in proficiencyIds
        else -> true
    } }

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
                feat.abilityBonusChoiceId?.let { choiceId ->
                    val choice = feat.abilityBonusChoice ?: return@let fixed
                    selections.featChoices[choiceId].orEmpty().fold(fixed) { scores, ability ->
                        choice.from.firstOrNull { ability in it }.orEmpty().entries.fold(scores) { current, (id, amount) ->
                            updateAbility(current, id, amount)
                        }
                    }
                } ?: fixed
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

    private fun blocking(code: LevelUpValidationCode, message: String) =
        LevelUpValidationIssue(code, message, LevelUpValidationSeverity.Blocking)

    private fun overrideable(code: LevelUpValidationCode, message: String) =
        LevelUpValidationIssue(code, message, LevelUpValidationSeverity.Overrideable)
}
