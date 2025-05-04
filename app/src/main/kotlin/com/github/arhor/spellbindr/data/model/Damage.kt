package com.github.arhor.spellbindr.data.model

data class Damage(
    val numberOfDice: Int,
    val diceType: DiceType,
    val damageType: DamageType,
) {
    override fun toString(): String = "$numberOfDice$diceType $damageType"
}
