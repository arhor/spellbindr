package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.Serializable


@Serializable
data class MultiClassing(
    val prerequisites: List<MultiClassingPreReq>? = null,
    val proficiencies: List<EntityRef>? = null,
    val proficiencyChoices: List<Choice>? = null
)
