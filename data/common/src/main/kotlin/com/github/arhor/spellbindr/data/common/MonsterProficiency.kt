package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class MonsterProficiency(
    val proficiency: EntityRef,
    val value: Int
)
