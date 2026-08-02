package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * A selectable feat from the bundled rules data.
 *
 * Only [effects] are intentionally machine-readable. Descriptive rules remain in [desc] and are
 * shown to the player without being inferred by progression calculation.
 */
@Serializable
data class Feat(
    val id: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<Prerequisite> = emptyList(),
    val effects: List<Effect> = emptyList(),
    val abilityBonusChoice: Choice.AbilityBonusChoice? = null,
    val languageChoice: Choice.ResourceListChoice? = null,
    val proficiencyChoice: Choice.OptionsArrayChoice? = null,
    val damageTypeChoice: Choice.OptionsArrayChoice? = null,
    val correlatesAbilityAndSavingThrow: Boolean = false,
    val repeatable: Boolean = false,
) {
    /** Stable persisted key for the feat-owned ability choice, when the feat has one. */
    val abilityBonusChoiceId: String?
        get() = abilityBonusChoice?.let { "$id:ability-bonus" }

    val languageChoiceId: String?
        get() = languageChoice?.let { "$id:language" }

    val proficiencyChoiceId: String?
        get() = proficiencyChoice?.let { "$id:proficiency" }

    val damageTypeChoiceId: String?
        get() = damageTypeChoice?.let { "$id:damage-type" }

    val correlatedAbilitySavingThrowChoiceId: String?
        get() = takeIf { correlatesAbilityAndSavingThrow }?.let { "$id:ability-and-saving-throw" }

    val ownedChoiceIds: Set<String>
        get() = if (correlatesAbilityAndSavingThrow) {
            setOfNotNull(correlatedAbilitySavingThrowChoiceId, languageChoiceId, damageTypeChoiceId)
        } else {
            setOfNotNull(
                abilityBonusChoiceId,
                languageChoiceId,
                proficiencyChoiceId,
                damageTypeChoiceId,
            )
        }
}
