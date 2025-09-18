package com.github.arhor.spellbindr.data.model.predefined

data class AbilityScores(
    val str: Int = MIN_SCORE,
    val dex: Int = MIN_SCORE,
    val con: Int = MIN_SCORE,
    val int: Int = MIN_SCORE,
    val wis: Int = MIN_SCORE,
    val cha: Int = MIN_SCORE,
) : Map<Ability, Int> by mapOf(
    Ability.STR to str,
    Ability.DEX to dex,
    Ability.CON to con,
    Ability.INT to int,
    Ability.WIS to wis,
    Ability.CHA to cha,
) {
    fun copy(scores: Map<Ability, Int>): AbilityScores = fromMap(this + scores)
    fun copy(vararg scores: Pair<Ability, Int>): AbilityScores = copy(scores.toMap())

    companion object {
        const val MIN_SCORE = 8

        fun fromMap(map: Map<Ability, Int>) = AbilityScores(
            str = map[Ability.STR] ?: MIN_SCORE,
            dex = map[Ability.DEX] ?: MIN_SCORE,
            con = map[Ability.CON] ?: MIN_SCORE,
            int = map[Ability.INT] ?: MIN_SCORE,
            wis = map[Ability.WIS] ?: MIN_SCORE,
            cha = map[Ability.CHA] ?: MIN_SCORE,
        )
    }
}
