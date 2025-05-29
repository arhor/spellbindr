package com.github.arhor.spellbindr.data.next.model

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val desc: String,
    val choose: Int,
    val type: String,
    val from: OptionSet,
)
