package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Monster(
    val id: String,
    val name: String,
    val actions: List<MonsterAction>? = null,
    val alignment: String,
    @SerialName("armor_class")
    val armorClass: List<ArmorClass>,
    @SerialName("challenge_rating")
    val challengeRating: Double,
    val charisma: Int,
    @SerialName("condition_immunities")
    val conditionImmunities: List<DomainEntityRef>,
    val constitution: Int,
    @SerialName("damage_immunities")
    val damageImmunities: List<String>,
    @SerialName("damage_resistances")
    val damageResistances: List<String>,
    @SerialName("damage_vulnerabilities")
    val damageVulnerabilities: List<String>,
    val dexterity: Int,
    val forms: List<DomainEntityRef>? = null,
    @SerialName("hit_dice")
    val hitDice: String,
    @SerialName("hit_points")
    val hitPoints: Int,
    @SerialName("hit_points_roll")
    val hitPointsRoll: String,
    val image: String? = null,
    val intelligence: Int,
    val languages: String,
    @SerialName("legendary_actions")
    val legendaryActions: List<LegendaryAction>? = null,
    val proficiencies: List<MonsterProficiency>,
    val reactions: List<Reaction>? = null,
    val senses: Sense,
    val size: String,
    @SerialName("special_abilities")
    val specialAbilities: List<SpecialAbility>? = null,
    val speed: Speed,
    val strength: Int,
    val subtype: String? = null,
    val type: String,
    val wisdom: Int,
    val xp: Int
)
