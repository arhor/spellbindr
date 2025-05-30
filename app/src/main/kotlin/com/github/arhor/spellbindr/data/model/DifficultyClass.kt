package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DifficultyClass(
    val dcType: EntityRef,
    val dcValue: Int,
    val successType: SuccessType
)
