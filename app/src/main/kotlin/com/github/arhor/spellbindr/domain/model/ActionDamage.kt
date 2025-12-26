package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


/**
 * Represents the damage dealt by an action.
 *
 * @property damageType The type of damage dealt.
 * @property damageAtCharacterLevel A map of character levels to damage values.
 */
@Serializable
data class ActionDamage(
    val damageType: DomainEntityRef,
    val damageAtCharacterLevel: Map<String, String>,
)
