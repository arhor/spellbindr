package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ProgressionState {

    @Serializable
    @SerialName("managed")
    data class Managed(val progression: CharacterProgression) : ProgressionState

    @Serializable
    @SerialName("unmanaged")
    data object Unmanaged : ProgressionState
}

@Serializable
data class CharacterProgression(
    val rulesetId: String = SUPPORTED_RULESET_ID,
    val referenceDataVersion: String,
    val origin: ProgressionOrigin,
    val levels: List<CharacterLevelRecord>,
    val overrides: CharacterOverrides = CharacterOverrides(),
) {
    val totalLevel: Int
        get() = levels.size

    val classLevels: Map<String, Int>
        get() = levels.groupingBy { it.classId }.eachCount()

    companion object {
        const val SUPPORTED_RULESET_ID = "srd-5e-2014-v1"
        const val BUNDLED_REFERENCE_DATA_VERSION = "srd-5e-2014-data-v1"
    }
}

@Serializable
enum class ProgressionOrigin {
    Guided,
    Reconciled,
}

@Serializable
data class CharacterLevelRecord(
    val characterLevel: Int,
    val classId: String,
    val classLevel: Int,
    val subclassId: String? = null,
    val hitPointGain: HitPointGain,
    val featureChoices: Map<String, Set<String>> = emptyMap(),
    val proficiencyChoices: List<ProficiencyChoiceSelection> = emptyList(),
    val abilityScoreDecision: AbilityScoreDecision? = null,
    /** Selections owned by a feat, keyed by the feat's stable choice id. */
    val featChoices: Map<String, Set<String>> = emptyMap(),
    val spellChanges: SpellChanges = SpellChanges(),
    /** Overrideable rules explicitly acknowledged while this level was taken. */
    val ruleAcknowledgements: Set<String> = emptySet(),
    /** Optional player-facing context for an acknowledgement or manual HP result. */
    val notes: String? = null,
)

@Serializable
data class ProficiencyChoiceSelection(
    val choiceId: String,
    val selectedProficiencyIds: Set<String>,
)

@Serializable
sealed interface HitPointGain {
    val rolledValue: Int

    @Serializable
    @SerialName("fixed")
    data class Fixed(override val rolledValue: Int) : HitPointGain

    @Serializable
    @SerialName("rolled")
    data class Rolled(override val rolledValue: Int) : HitPointGain

    @Serializable
    @SerialName("manual")
    data class Manual(override val rolledValue: Int) : HitPointGain
}

@Serializable
sealed interface AbilityScoreDecision {

    @Serializable
    @SerialName("ability-score-increase")
    data class Increase(val increases: Map<AbilityId, Int>) : AbilityScoreDecision

    @Serializable
    @SerialName("feat")
    data class Feat(val featId: String) : AbilityScoreDecision
}

@Serializable
data class SpellChanges(
    val learned: Set<ClassSpellRef> = emptySet(),
    val replaced: Set<SpellReplacement> = emptySet(),
    val addedToSpellbook: Set<ClassSpellRef> = emptySet(),
)

@Serializable
data class ClassSpellRef(
    val classId: String,
    val spellId: String,
)

@Serializable
data class SpellReplacement(
    val classId: String,
    val removedSpellId: String,
    val learnedSpellId: String,
)

@Serializable
data class CharacterOverrides(
    val proficiencyBonus: ValueOverride<Int>? = null,
    val maximumHitPoints: ValueOverride<Int>? = null,
    val abilityScores: ValueOverride<AbilityScores>? = null,
    val sharedSpellSlots: ValueOverride<Map<Int, Int>>? = null,
    val pactSlots: ValueOverride<PactSlotState>? = null,
)

@Serializable
data class ValueOverride<T>(
    val value: T,
    val note: String? = null,
)
