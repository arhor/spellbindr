package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubclassSpell(
    val spell: String,
    val prerequisites: List<Prerequisite>? = null,
)
