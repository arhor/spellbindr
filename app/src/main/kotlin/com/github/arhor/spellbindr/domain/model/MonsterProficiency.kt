package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class MonsterProficiency(
    val proficiency: EntityRef,
    val value: Int
)
