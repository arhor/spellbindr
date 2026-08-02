package com.github.arhor.spellbindr.domain.model

/** Versioned, structured rules consumed by level-up planning and materialization. */
object LevelUpReferenceRules {

    const val rulesetId: String = CharacterProgression.SUPPORTED_RULESET_ID
    const val referenceDataVersion: String = CharacterProgression.BUNDLED_REFERENCE_DATA_VERSION
    const val maximumCharacterLevel: Int = 20

    val experienceThresholds: Map<Int, Int> = mapOf(
        1 to 0,
        2 to 300,
        3 to 900,
        4 to 2_700,
        5 to 6_500,
        6 to 14_000,
        7 to 23_000,
        8 to 34_000,
        9 to 48_000,
        10 to 64_000,
        11 to 85_000,
        12 to 100_000,
        13 to 120_000,
        14 to 140_000,
        15 to 165_000,
        16 to 195_000,
        17 to 225_000,
        18 to 265_000,
        19 to 305_000,
        20 to 355_000,
    )

    /** The multiclass spell-slot table; Pact Magic slots are deliberately excluded. */
    val sharedSpellSlots: Map<Int, Map<Int, Int>> = mapOf(
        1 to mapOf(1 to 2),
        2 to mapOf(1 to 3),
        3 to mapOf(1 to 4, 2 to 2),
        4 to mapOf(1 to 4, 2 to 3),
        5 to mapOf(1 to 4, 2 to 3, 3 to 2),
        6 to mapOf(1 to 4, 2 to 3, 3 to 3),
        7 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 1),
        8 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 2),
        9 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 1),
        10 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2),
        11 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1),
        12 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1),
        13 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1),
        14 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1),
        15 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1),
        16 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1),
        17 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 2, 6 to 1, 7 to 1, 8 to 1, 9 to 1),
        18 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 1, 7 to 1, 8 to 1, 9 to 1),
        19 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 2, 7 to 1, 8 to 1, 9 to 1),
        20 to mapOf(1 to 4, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 2, 7 to 2, 8 to 1, 9 to 1),
    )

    val pactMagic: PactMagicPolicy = PactMagicPolicy(classId = "warlock")

    private val standardAsiLevels: Set<Int> = setOf(4, 8, 12, 16, 19)
    private val fighterAsiLevels: Set<Int> = setOf(4, 6, 8, 12, 14, 16, 19)
    private val rogueAsiLevels: Set<Int> = setOf(4, 8, 10, 12, 16, 19)

    val classPolicies: Map<String, CharacterClassLevelUpPolicy> = mapOf(
        "barbarian" to nonCasterPolicy(subclassLevel = 3, abilityScoreImprovementLevels = standardAsiLevels),
        "bard" to knownCasterPolicy(subclassLevel = 3),
        "cleric" to preparedCasterPolicy(subclassLevel = 1, abilityId = AbilityIds.WIS),
        "druid" to preparedCasterPolicy(subclassLevel = 2, abilityId = AbilityIds.WIS),
        "fighter" to nonCasterPolicy(subclassLevel = 3, abilityScoreImprovementLevels = fighterAsiLevels).copy(
            subclassCasterContributions = mapOf("eldritch-knight" to CasterContribution.Third),
        ),
        "monk" to nonCasterPolicy(subclassLevel = 3, abilityScoreImprovementLevels = standardAsiLevels),
        "paladin" to preparedCasterPolicy(
            subclassLevel = 3,
            abilityId = AbilityIds.CHA,
            contribution = CasterContribution.Half,
            preparationLevelDivisor = 2,
        ),
        "ranger" to knownCasterPolicy(subclassLevel = 3, contribution = CasterContribution.Half),
        "rogue" to nonCasterPolicy(subclassLevel = 3, abilityScoreImprovementLevels = rogueAsiLevels).copy(
            subclassCasterContributions = mapOf("arcane-trickster" to CasterContribution.Third),
        ),
        "sorcerer" to knownCasterPolicy(subclassLevel = 1),
        "warlock" to CharacterClassLevelUpPolicy(
            subclass = SubclassAcquisitionPolicy(level = 1),
            abilityScoreImprovement = AbilityScoreImprovementPolicy(standardAsiLevels),
            casterContribution = CasterContribution.Pact,
            spells = SpellLearningPolicy.Known(replacementStartsAtLevel = 2),
        ),
        "wizard" to CharacterClassLevelUpPolicy(
            subclass = SubclassAcquisitionPolicy(level = 2),
            abilityScoreImprovement = AbilityScoreImprovementPolicy(standardAsiLevels),
            casterContribution = CasterContribution.Full,
            spells = SpellLearningPolicy.Spellbook(
                spellsAtFirstLevel = 6,
                spellsAddedPerLevel = 2,
                preparation = SpellPreparationPolicy(abilityId = AbilityIds.INT, levelDivisor = 1),
            ),
        ),
    )

    fun policyFor(classId: String): CharacterClassLevelUpPolicy? = classPolicies[classId]

    private fun nonCasterPolicy(
        subclassLevel: Int,
        abilityScoreImprovementLevels: Set<Int>,
    ) = CharacterClassLevelUpPolicy(
        subclass = SubclassAcquisitionPolicy(level = subclassLevel),
        abilityScoreImprovement = AbilityScoreImprovementPolicy(abilityScoreImprovementLevels),
    )

    private fun knownCasterPolicy(
        subclassLevel: Int,
        contribution: CasterContribution = CasterContribution.Full,
    ) = CharacterClassLevelUpPolicy(
        subclass = SubclassAcquisitionPolicy(level = subclassLevel),
        abilityScoreImprovement = AbilityScoreImprovementPolicy(standardAsiLevels),
        casterContribution = contribution,
        spells = SpellLearningPolicy.Known(replacementStartsAtLevel = 2),
    )

    private fun preparedCasterPolicy(
        subclassLevel: Int,
        abilityId: AbilityId,
        contribution: CasterContribution = CasterContribution.Full,
        preparationLevelDivisor: Int = 1,
    ) = CharacterClassLevelUpPolicy(
        subclass = SubclassAcquisitionPolicy(level = subclassLevel),
        abilityScoreImprovement = AbilityScoreImprovementPolicy(standardAsiLevels),
        casterContribution = contribution,
        spells = SpellLearningPolicy.Prepared(
            SpellPreparationPolicy(abilityId = abilityId, levelDivisor = preparationLevelDivisor),
        ),
    )
}

data class CharacterClassLevelUpPolicy(
    val subclass: SubclassAcquisitionPolicy,
    val abilityScoreImprovement: AbilityScoreImprovementPolicy,
    val casterContribution: CasterContribution = CasterContribution.None,
    val spells: SpellLearningPolicy = SpellLearningPolicy.None,
    /** Subclasses such as Eldritch Knight and Arcane Trickster can opt into multiclass casting. */
    val subclassCasterContributions: Map<String, CasterContribution> = emptyMap(),
)

data class SubclassAcquisitionPolicy(
    val level: Int,
    val selectionIsSticky: Boolean = true,
)

data class AbilityScoreImprovementPolicy(
    val levels: Set<Int>,
    val abilityPoints: Int = 2,
    val maximumAbilityScore: Int = 20,
    val allowsFeat: Boolean = true,
)

sealed interface CasterContribution {
    data object None : CasterContribution
    data object Full : CasterContribution
    data object Half : CasterContribution
    data object Third : CasterContribution
    data object Pact : CasterContribution
}

sealed interface SpellLearningPolicy {
    data object None : SpellLearningPolicy
    data class Known(val replacementStartsAtLevel: Int) : SpellLearningPolicy
    data class Prepared(val preparation: SpellPreparationPolicy) : SpellLearningPolicy
    data class Spellbook(
        val spellsAtFirstLevel: Int,
        val spellsAddedPerLevel: Int,
        val preparation: SpellPreparationPolicy,
    ) : SpellLearningPolicy
}

data class SpellPreparationPolicy(
    val abilityId: AbilityId,
    val levelDivisor: Int,
    val minimumPreparedSpells: Int = 1,
)

data class PactMagicPolicy(
    val classId: String,
    val slotsRefreshOnShortRest: Boolean = true,
)

/** Descriptive features never participate in calculations unless they carry a structured [Choice]. */
sealed interface FeatureCapability {
    data object Descriptive : FeatureCapability
    data class Selection(
        val choiceId: String,
        val choose: Int,
        val kind: FeatureChoiceKind,
    ) : FeatureCapability
}

enum class FeatureChoiceKind {
    Options,
    Nested,
    Proficiency,
    AbilityBonus,
    Other,
}

fun Feature.levelUpCapability(): FeatureCapability = choice?.let {
    FeatureCapability.Selection(
        choiceId = "$id:choice",
        choose = it.choose,
        kind = when (it) {
            is Choice.OptionsArrayChoice -> FeatureChoiceKind.Options
            is Choice.NestedChoice -> FeatureChoiceKind.Nested
            is Choice.ProficiencyChoice -> FeatureChoiceKind.Proficiency
            is Choice.AbilityBonusChoice -> FeatureChoiceKind.AbilityBonus
            else -> FeatureChoiceKind.Other
        },
    )
} ?: FeatureCapability.Descriptive
