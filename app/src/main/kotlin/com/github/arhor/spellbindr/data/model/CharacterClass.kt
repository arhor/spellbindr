package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterClass(
    val id: String,
    val name: String,
//    val multiClassing: MultiClassing,
    val hitDie: Int,
    val proficiencies: List<EntityRef>,
//    val proficiencyChoices: List<Choice>,
    val savingThrows: List<EntityRef>,
    val spellcasting: Spellcasting? = null,
    val startingEquipment: List<EquipmentRef>,
//    val startingEquipmentOptions: List<Choice>,
    val subclasses: List<EntityRef>
)
