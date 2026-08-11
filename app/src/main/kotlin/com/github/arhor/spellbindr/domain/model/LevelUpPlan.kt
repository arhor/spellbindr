package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * A serializable, one-level draft. The expected level protects confirmation from applying a stale draft.
 *
 * It intentionally stores ids rather than asset objects so it can be retained by a SavedStateHandle and rebuilt
 * against a newer reference-data snapshot.
 */
@Serializable
data class LevelUpPlan(
    val expectedTotalLevel: Int,
    val rulesetId: String,
    val referenceDataVersion: String,
    val selectedClassId: String? = null,
    val selections: LevelUpSelections = LevelUpSelections(),
)

@Serializable
data class LevelUpSelections(
    val subclassId: String? = null,
    val featureChoices: Map<String, Set<String>> = emptyMap(),
    val proficiencyChoices: Map<String, Set<String>> = emptyMap(),
    val hitPointGain: HitPointGain? = null,
    val abilityScoreDecision: AbilityScoreDecision? = null,
    val featChoices: Map<String, Set<String>> = emptyMap(),
    val spellChanges: SpellChanges = SpellChanges(),
    val acknowledgedIssueCodes: Set<String> = emptySet(),
    val note: String? = null,
)

@Serializable
enum class LevelUpValidationSeverity {
    Blocking,
    Overrideable,
    Informational,
}

@Serializable
enum class LevelUpValidationCode {
    UnmanagedCharacter,
    StaleProgression,
    UnsupportedRuleset,
    ReferenceDataVersionMismatch,
    CorruptProgression,
    MissingClass,
    MissingClassLevel,
    MissingSubclass,
    MissingFeature,
    MaximumCharacterLevel,
    MaximumClassLevel,
    MulticlassPrerequisite,
    ExperienceThreshold,
    SubclassRequired,
    StickySubclass,
    ChoiceRequired,
    InvalidChoice,
    HitPointGainRequired,
    InvalidHitPointGain,
    AbilityScoreDecisionRequired,
    InvalidAbilityScoreIncrease,
    FeatRequired,
    FeatPrerequisite,
    FeatAlreadySelected,
    UnsupportedFeatDecision,
    MissingFeat,
    SpellPolicy,
}

@Serializable
data class LevelUpValidationIssue(
    val code: LevelUpValidationCode,
    val message: String,
    val severity: LevelUpValidationSeverity,
    /** Stable identity of this particular finding, when a code can occur more than once. */
    val findingId: String? = null,
) {
    val acknowledgementId: String
        get() = findingId ?: code.name
}

@Serializable
sealed interface LevelUpRequirement {
    val id: String

    @Serializable
    data class ClassSelection(
        override val id: String = "class-selection",
        val eligibleClassIds: List<String>,
        val selectedClassId: String?,
        /** Per-class rule status; ineligible prerequisite entries remain selectable through acknowledgement. */
        val eligibility: List<LevelUpClassEligibility> = emptyList(),
    ) : LevelUpRequirement

    @Serializable
    data class SubclassSelection(
        override val id: String,
        val classId: String,
        val options: List<LevelUpChoiceOption>,
        val selectedSubclassId: String?,
    ) : LevelUpRequirement

    @Serializable
    data class ChoiceSelection(
        override val id: String,
        val sourceId: String,
        val label: String,
        val choice: Choice,
        val selectedOptionIds: Set<String>,
        val category: LevelUpChoiceCategory,
        val options: List<LevelUpChoiceOption> = emptyList(),
    ) : LevelUpRequirement

    @Serializable
    data class HitPoints(
        override val id: String = "hit-points",
        val hitDie: Int,
        val fixedGain: Int = hitDie / 2 + 1,
        val selectedGain: HitPointGain?,
    ) : LevelUpRequirement

    @Serializable
    data class AbilityScoreImprovement(
        override val id: String,
        val classId: String,
        val abilityPoints: Int,
        val maximumAbilityScore: Int,
        val allowsFeat: Boolean,
        val eligibleFeatIds: List<String> = emptyList(),
        val featEligibility: List<LevelUpFeatEligibility> = emptyList(),
        val selectedDecision: AbilityScoreDecision?,
    ) : LevelUpRequirement

    /** The spell subsystem owns exact spell selection counts and legality. */
    @Serializable
    data class SpellDecisions(
        override val id: String,
        val classId: String,
        val classLevel: Int,
        val policyId: String,
        val changes: SpellChanges,
        val requiredCantripCount: Int = 0,
        val cantripCandidates: List<LevelUpSpellOption> = emptyList(),
        val requiredKnownSpellCount: Int = 0,
        val knownSpellCandidates: List<LevelUpSpellOption> = emptyList(),
        val featureSpellGrants: List<LevelUpFeatureSpellGrantRequirement> = emptyList(),
        val replacement: LevelUpSpellReplacementRequirement? = null,
        val requiredSpellbookAdditionCount: Int = 0,
        val spellbookCandidates: List<LevelUpSpellOption> = emptyList(),
        /** A mutable prepared list may contain at most this many spells, when applicable. */
        val preparationCapacity: Int? = null,
    ) : LevelUpRequirement

    @Serializable
    data class Acknowledgement(
        override val id: String,
        val issue: LevelUpValidationIssue,
        val acknowledged: Boolean,
    ) : LevelUpRequirement
}

@Serializable
data class LevelUpClassEligibility(
    val classId: String,
    val eligible: Boolean,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class LevelUpFeatEligibility(
    val featId: String,
    val eligible: Boolean,
    val reasons: List<String> = emptyList(),
    val deferredDecision: LevelUpDeferredFeatDecision? = null,
)

@Serializable
enum class LevelUpDeferredFeatDecision {
    SpellSelection,
    ManeuverSelection,
}

@Serializable
enum class LevelUpChoiceCategory {
    Feature,
    Proficiency,
    Feat,
}

@Serializable
data class LevelUpChoiceOption(
    val id: String,
    val label: String = id,
)

@Serializable
data class LevelUpSpellOption(
    val spellId: String,
    val name: String,
    val level: Int,
)

@Serializable
data class LevelUpSpellReplacementRequirement(
    val sourceCandidates: List<LevelUpSpellOption>,
    val replacementCandidates: List<LevelUpSpellOption>,
    val selectedSourceSpellId: String? = null,
    val selectedReplacementSpellId: String? = null,
)

@Serializable
data class LevelUpFeatureSpellGrantRequirement(
    val featureId: String,
    val label: String,
    val requiredCount: Int,
    val candidates: List<LevelUpSpellOption>,
    val selectedSpellIds: Set<String>,
)

@Serializable
data class LevelUpHitDicePool(
    val dieSize: Int,
    val total: Int,
)

@Serializable
data class LevelUpSnapshot(
    val totalLevel: Int,
    val classLevels: Map<String, Int>,
    val classDisplayName: String,
    val proficiencyBonus: Int,
    val abilityScores: AbilityScores,
    val maximumHitPoints: Int,
    val hitDicePools: List<LevelUpHitDicePool>,
    val proficiencyIds: Set<String>,
    val savingThrowAbilityIds: Set<AbilityId>,
    val featureIds: Set<String>,
    val sharedCasterLevel: Int,
    val sharedSpellSlots: Map<Int, Int>,
    /** Pact Magic never contributes to the shared multiclass slot table. */
    val pactMagic: LevelUpPactMagicCapacity? = null,
    val languageIds: Set<String> = emptySet(),
    /** Maneuvers selected by each feat owner, keyed by the stable feat id. */
    val featManeuvers: Map<String, Set<String>> = emptyMap(),
    val resources: List<LevelUpResource> = emptyList(),
)

@Serializable
data class LevelUpResource(
    val id: String,
    val name: String,
    val maximum: Int,
    val recovery: ResourceRecovery = ResourceRecovery.ShortOrLongRest,
)

@Serializable
enum class ResourceRecovery { ShortRest, LongRest, ShortOrLongRest }

@Serializable
data class LevelUpPactMagicCapacity(
    val slotLevel: Int,
    val slots: Int,
)

@Serializable
data class LevelUpPreview(
    val before: LevelUpSnapshot,
    val after: LevelUpSnapshot,
    val requirements: List<LevelUpRequirement>,
    val validations: List<LevelUpValidationIssue>,
) {
    val canConfirm: Boolean
        get() = validations.none { it.severity == LevelUpValidationSeverity.Blocking } &&
            validations.filter { it.severity == LevelUpValidationSeverity.Overrideable }
                .all { issue -> requirements.filterIsInstance<LevelUpRequirement.Acknowledgement>()
                    .any { it.id == issue.acknowledgementId && it.acknowledged } }
}

/** Reference data used by the pure engine; repositories adapt their loaded assets into this value. */
data class LevelUpReferenceData(
    val classes: List<CharacterClass>,
    val features: List<Feature>,
    val feats: List<Feat> = emptyList(),
    val referenceDataVersion: String = LevelUpReferenceRules.referenceDataVersion,
    val spells: List<Spell> = emptyList(),
    val languages: List<Language> = emptyList(),
) {
    val classesById: Map<String, CharacterClass> = classes.associateBy(CharacterClass::id)
    val featuresById: Map<String, Feature> = features.associateBy(Feature::id)
    val featsById: Map<String, Feat> = feats.associateBy(Feat::id)
    val spellsById: Map<String, Spell> = spells.associateBy(Spell::id)
    val languagesById: Map<String, Language> = languages.associateBy(Language::id)
}
