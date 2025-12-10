package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MonsterProficiency(
    val proficiency: String,
    val value: Int
)
