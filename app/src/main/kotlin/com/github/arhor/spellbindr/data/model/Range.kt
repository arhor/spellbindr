package com.github.arhor.spellbindr.data.model

sealed class Range() {
    data object Touch : Range()

    data object Unlimited : Range()

    data class Self(val area: AreaOfEffect? = null) : Range()

    data class Feet(val distance: Int, val area: AreaOfEffect? = null) : Range()

    data class Miles(val distance: Int, val area: AreaOfEffect? = null) : Range()

    data class Special(val description: String) : Range()
}
