package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


/**
 * Represents the damage inflicted by a spell.
 *
 * @property damageType The type of damage inflicted by the spell (e.g., fire, cold, necrotic).
 *                      This is an optional reference to a damage type representing the damage type.
 * @property damageAtSlotLevel A map representing the damage dealt by the spell at different slot levels.
 *                             The keys are slot levels (e.g., "1", "2", "cantrip"), and the values are
 *                             strings describing the damage (e.g., "1d6", "2d8 + Spellcasting Ability Modifier").
 *                             This is optional.
 * @property damageAtCharacterLevel A map representing how the spell's damage scales with character level.
 *                                  The keys are character levels (e.g., "1", "5", "11"), and the values
 *                                  are strings describing the damage at that level. This is typically
 *                                  used for cantrips. This is optional.
 * @see [DamageType] for more information about damage types.
 */
@Serializable
data class SpellDamage(
    val damageType: EntityRef? = null,
    val damageAtSlotLevel: Map<String, String>? = null,
    val damageAtCharacterLevel: Map<String, String>? = null,
)
