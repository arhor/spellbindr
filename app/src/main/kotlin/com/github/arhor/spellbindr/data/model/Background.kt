package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Background(
    val id: String,
    val name: String,
    val startingProficiencies: List<EntityRef>,
    val languageOptions: Choice,
    val startingEquipment: List<EquipmentRef>,
    val startingEquipmentOptions: List<Choice>,
    val feature: GenericInfo,
    val personalityTraits: Choice,
    val ideals: Choice,
    val bonds: Choice,
    val flaws: Choice,
)
