package com.github.arhor.spellbindr.data.classes

import com.github.arhor.spellbindr.data.common.EntityRef
import com.github.arhor.spellbindr.data.common.EquipmentRef
import com.github.arhor.spellbindr.data.common.Spellcasting
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
