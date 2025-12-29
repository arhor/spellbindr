package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.ArmorClass
import com.github.arhor.spellbindr.data.model.LegendaryAction
import com.github.arhor.spellbindr.data.model.MonsterProficiency
import com.github.arhor.spellbindr.data.model.Reaction
import com.github.arhor.spellbindr.data.model.SpecialAbility
import com.github.arhor.spellbindr.data.model.Speed
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    val id: String,
    val name: String,
    val actions: List<MonsterAction>? = null,
    val alignment: String,
    val armorClass: List<ArmorClass>,
    val challengeRating: Double,
    val charisma: Int,
    val conditionImmunities: List<EntityRef>,
    val constitution: Int,
    val damageImmunities: List<String>,
    val damageResistances: List<String>,
    val damageVulnerabilities: List<String>,
    val dexterity: Int,
    val forms: List<EntityRef>? = null,
    val hitDice: String,
    val hitPoints: Int,
    val hitPointsRoll: String,
    val image: String? = null,
    val intelligence: Int,
    val languages: String,
    val legendaryActions: List<LegendaryAction>? = null,
    val proficiencies: List<MonsterProficiency>,
    val reactions: List<Reaction>? = null,
    val senses: Sense,
    val size: String,
    val specialAbilities: List<SpecialAbility>? = null,
    val speed: Speed,
    val strength: Int,
    val subtype: String? = null,
    val type: String,
    val wisdom: Int,
    val xp: Int
)
