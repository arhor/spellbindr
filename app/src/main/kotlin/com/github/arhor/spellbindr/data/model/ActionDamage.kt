package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.Serializable

/**
 * Represents the damage dealt by an action.
 *
 * @property damageType The type of damage dealt.
 * @property damageAtCharacterLevel A map of character levels to damage values.
 */
@Serializable
data class ActionDamage(
    val damageType: EntityRef,
    val damageAtCharacterLevel: Map<String, String>,
)
