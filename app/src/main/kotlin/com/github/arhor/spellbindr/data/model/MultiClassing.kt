package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MultiClassing(
    val prerequisites: List<MultiClassingPreReq>? = null,
    val proficiencies: List<EntityRef>? = null,
    val proficiencyChoices: List<Choice>? = null
)
