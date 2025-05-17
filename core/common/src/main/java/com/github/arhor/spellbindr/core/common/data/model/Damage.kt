package com.github.arhor.spellbindr.core.common.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Damage(
    val numberOfDice: Int,
    val diceType: DiceType,
    val damageType: DamageType,
) {
    override fun toString(): String = "$numberOfDice$diceType $damageType"
}
