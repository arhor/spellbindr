package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Spellcasting(
    val info: List<GenericInfo>,
    val level: Int,
    val spellcastingAbility: EntityRef,
)
