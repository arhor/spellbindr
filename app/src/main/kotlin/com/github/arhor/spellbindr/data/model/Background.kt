package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Background(
    val id: String,
    val name: String,
    val languageOptions: Choice,
    val startingProficiencies: List<EntityRef>,
    val startingEquipment: List<EquipmentRef>,
    val startingEquipmentOptions: List<Choice>,
    val feature: GenericInfo,
    val personalityTraits: Choice,
    val ideals: Choice,
    val bonds: Choice,
    val flaws: Choice,
)
