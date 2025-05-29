@file:Suppress("unused")

package com.github.arhor.spellbindr.data.newModel

import com.github.arhor.spellbindr.data.next.model.AreaOfEffect
import com.github.arhor.spellbindr.data.next.model.Choice
import com.github.arhor.spellbindr.data.next.model.DifficultyClass
import com.github.arhor.spellbindr.data.next.model.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbilityScore(
    val id: String,
    val name: String,
    val desc: List<String>,
    val fullName: String,
    val skills: List<EntityRef>
)

@Serializable
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbr: String,
)

@Serializable
data class EquipmentRef(
    val equipment: EntityRef,
    val quantity: Int
)

@Serializable
data class GenericInfo(
    val name: String,
    val desc: List<String>
)

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
    val flaws: Choice
)

@Serializable
data class Spellcasting(
    val info: List<GenericInfo>,
    val level: Int,
    val spellcastingAbility: EntityRef
)

@Serializable
data class MultiClassingPreReq(
    val abilityScore: EntityRef,
    val minimumScore: Int
)

@Serializable
data class MultiClassing(
    val prerequisites: List<MultiClassingPreReq>? = null,
    val prerequisiteOptions: Choice? = null,
    val proficiencies: List<EntityRef>? = null,
    val proficiencyChoices: List<Choice>? = null
)

@Serializable
data class Damage(
    val damageDice: String,
    val damageType: EntityRef
)

@Serializable
data class EquipmentArmorClass(
    val base: Int,
    @SerialName("dex_bonus")
    val dexBonus: Boolean,
    @SerialName("max_bonus")
    val maxBonus: Int? = null
)

@Serializable
data class Content(
    val item: EntityRef,
    val quantity: Int
)

@Serializable
data class Cost(
    val quantity: Int,
    val unit: String
)

@Serializable
data class Range(
    val long: Int? = null,
    val normal: Int
)

@Serializable
data class EquipmentSpeed(
    val quantity: Int,
    val unit: String
)

@Serializable
data class TwoHandedDamage(
    @SerialName("damage_dice")
    val damageDice: String,
    val damageType: EntityRef
)

@Serializable
data class Equipment(
    val id: String,
    val name: String,
    val desc: List<String>,
    val armorClass: EquipmentArmorClass? = null,
    val capacity: String? = null,
    val contents: List<Content>? = null,
    val cost: Cost,
    val damage: Damage? = null,
    val properties: List<EntityRef>? = null,
    val quantity: Int? = null,
    val range: Range? = null,
    val special: List<String>? = null,
    val speed: EquipmentSpeed? = null,
    val stealthDisadvantage: Boolean? = null,
    val strMinimum: Int? = null,
    val throwRange: Range? = null,
    val twoHandedDamage: TwoHandedDamage? = null,
    val weight: Double? = null,
    val categories: List<EquipmentCategory>,
)

enum class EquipmentCategory {
    @SerialName("weapon")
    WEAPON,

    @SerialName("armor")
    ARMOR,

    @SerialName("tool")
    TOOL,

    @SerialName("gear")
    GEAR,

    @SerialName("holy-symbol")
    HOLY_SYMBOL,

    @SerialName("standard")
    STANDARD,

    @SerialName("musical-instrument")
    MUSICAL_INSTRUMENT,

    @SerialName("gaming-set")
    GAMING_SET,

    @SerialName("other")
    OTHER,

    @SerialName("arcane-focus")
    ARCANE_FOCUS,

    @SerialName("druidic-focus")
    DRUIDIC_FOCUS,

    @SerialName("kit")
    KIT,

    @SerialName("simple")
    SIMPLE,

    @SerialName("martial")
    MARTIAL,

    @SerialName("ranged")
    RANGED,

    @SerialName("melee")
    MELEE,

    @SerialName("shield")
    SHIELD,
}

@Serializable
data class FeatPrerequisite(
    val abilityScore: EntityRef,
    val minimumScore: Int
)

@Serializable
data class Feat(
    val id: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<FeatPrerequisite>
)

@Serializable
data class LevelPrerequisite(
    val type: String,
    val level: Int
)

@Serializable
data class FeaturePrerequisite(
    val type: String,
    val feature: String
)

@Serializable
data class SpellPrerequisite(
    val type: String,
    val spell: String
)

@Serializable
sealed class CommonPrerequisite {
    @Serializable
    data class LevelPrerequisiteWrapper(val level: LevelPrerequisite) : CommonPrerequisite()

    @Serializable
    data class FeaturePrerequisiteWrapper(val feature: FeaturePrerequisite) : CommonPrerequisite()

    @Serializable
    data class SpellPrerequisiteWrapper(val spell: SpellPrerequisite) : CommonPrerequisite()
}

@Serializable
data class FeatureSpecific(
    @SerialName("subfeature_options")
    val subfeatureOptions: Choice? = null,
    @SerialName("expertise_options")
    val expertiseOptions: Choice? = null,
    @SerialName("terrain_type_options")
    val terrainTypeOptions: Choice? = null,
    @SerialName("enemy_type_options")
    val enemyTypeOptions: Choice? = null,
    val invocations: List<EntityRef>? = null
)

@Serializable
data class Feature(
    val id: String,
    val name: String,
    val desc: List<String>,
    @SerialName("class")
    val clazz: EntityRef,
    val parent: EntityRef? = null,
    val level: Int,
    val prerequisites: List<CommonPrerequisite>? = null,
    val reference: String? = null,
    val subclass: EntityRef? = null,
    @SerialName("feature_specific")
    val featureSpecific: FeatureSpecific? = null
)

@Serializable
data class Language(
    val id: String,
    val name: String,
    val desc: String,
    val type: String,
    val script: String,
    @SerialName("typical_speakers")
    val typicalSpeakers: List<String>
)

@Serializable
data class Rarity(
    val name: String
)

@Serializable
data class MagicItem(
    val id: String,
    val name: String,
    val desc: List<String>,
    val image: String? = null,
    val rarity: Rarity,
    val variant: Boolean,
    val variants: List<EntityRef>,
    @SerialName("equipment_category")
    val equipmentCategory: EntityRef
)

@Serializable
data class MagicSchool(
    val id: String,
    val name: String,
    val desc: String
)

@Serializable
data class ActionItem(
    @SerialName("action_name")
    val actionName: String,
    val count: String,
    val type: String
)

@Serializable
data class ActionUsage(
    val type: String,
    val dice: String? = null,
    @SerialName("min_value")
    val minValue: Int? = null
)

@Serializable
data class Action(
    val name: String,
    val desc: String,
    @SerialName("attack_bonus")
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null,
    val options: Choice? = null,
    val usage: ActionUsage? = null,
    @SerialName("multiattack_type")
    val multiattackType: String,
    val actions: List<ActionItem>,
    @SerialName("action_options")
    val actionOptions: Choice
)

@Serializable
sealed class ArmorClass {
    abstract val type: String
    abstract val value: Int
    open val desc: String? get() = null

    @Serializable
    @SerialName("dex")
    data class ArmorClassDex(
        override val type: String = "dex",
        override val value: Int,
        override val desc: String? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("natural")
    data class ArmorClassNatural(
        override val type: String = "natural",
        override val value: Int,
        override val desc: String? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("armor")
    data class ArmorClassArmor(
        override val type: String = "armor",
        override val value: Int,
        override val desc: String? = null,
        val armor: List<EntityRef>? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("spell")
    data class ArmorClassSpell(
        override val type: String = "spell",
        override val value: Int,
        override val desc: String? = null,
        val spell: EntityRef
    ) : ArmorClass()

    @Serializable
    @SerialName("condition")
    data class ArmorClassCondition(
        override val type: String = "condition",
        override val value: Int,
        override val desc: String? = null,
        val condition: EntityRef
    ) : ArmorClass()
}

@Serializable
data class LegendaryAction(
    val name: String,
    val desc: String,
    @SerialName("attack_bonus")
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null
)

@Serializable
data class MonsterProficiency(
    val proficiency: EntityRef,
    val value: Int
)

@Serializable
data class Reaction(
    val name: String,
    val desc: String,
    val dc: DifficultyClass? = null
)

@Serializable
data class Sense(
    val blindsight: String? = null,
    val darkvision: String? = null,
    @SerialName("passive_perception")
    val passivePerception: Int,
    val tremorsense: String? = null,
    val truesight: String? = null
)

@Serializable
data class SpecialAbilityUsage(
    val type: String,
    val times: Int? = null,
    @SerialName("rest_types")
    val restTypes: List<String>? = null
)

@Serializable
data class SpecialAbilitySpell(
    val name: String,
    val level: Int,
    val notes: String? = null,
    val usage: SpecialAbilityUsage? = null
)

@Serializable
data class SpecialAbilitySpellcasting(
    val level: Int? = null,
    val ability: EntityRef,
    val dc: Int? = null,
    val modifier: Int? = null,
    @SerialName("components_required")
    val componentsRequired: List<String>,
    val school: String? = null,
    val slots: Map<String, Int>? = null,
    val spells: List<SpecialAbilitySpell>
)

@Serializable
data class SpecialAbility(
    val name: String,
    val desc: String,
    @SerialName("attack_bonus")
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null,
    val spellcasting: SpecialAbilitySpellcasting? = null,
    val usage: SpecialAbilityUsage
)

@Serializable
data class Speed(
    val burrow: String? = null,
    val climb: String? = null,
    val fly: String? = null,
    val hover: Boolean? = null,
    val swim: String? = null,
    val walk: String? = null
)

@Serializable
data class Monster(
    val id: String,
    val name: String,
    val actions: List<Action>? = null,
    val alignment: String,
    @SerialName("armor_class")
    val armorClass: List<ArmorClass>,
    @SerialName("challenge_rating")
    val challengeRating: Double,
    val charisma: Int,
    @SerialName("condition_immunities")
    val conditionImmunities: List<EntityRef>,
    val constitution: Int,
    @SerialName("damage_immunities")
    val damageImmunities: List<String>,
    @SerialName("damage_resistances")
    val damageResistances: List<String>,
    @SerialName("damage_vulnerabilities")
    val damageVulnerabilities: List<String>,
    val dexterity: Int,
    val forms: List<EntityRef>? = null,
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

@Serializable
data class Reference(
    val id: String,
    val name: String,
    val type: String
)

@Serializable
data class GenericProficiency(
    val id: String,
    val name: String,
    val type: String,
    val races: List<EntityRef>? = null,
    val classes: List<EntityRef>? = null,
    val reference: Reference
)

@Serializable
data class RaceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)

@Serializable
data class Race(
    @SerialName("ability_bonus_options")
    val abilityBonusOptions: Choice? = null,
    val abilityBonuses: List<RaceAbilityBonus>,
    val age: String,
    val alignment: String,
    val id: String,
    @SerialName("language_desc")
    val languageDesc: String,
    val languageOptions: Choice,
    val languages: List<EntityRef>,
    val name: String,
    val size: String,
    @SerialName("size_description")
    val sizeDescription: String,
    val speed: Int,
    val startingProficiencies: List<EntityRef>? = null,
    @SerialName("starting_proficiency_options")
    val startingProficiencyOptions: Choice? = null,
    val subraces: List<EntityRef>? = null,
    val traits: List<EntityRef>? = null
)

@Serializable
data class Rule(
    val id: String,
    val name: String,
    val desc: String,
    val subsections: List<EntityRef>
)

@Serializable
data class RuleSection(
    val id: String,
    val name: String,
    val desc: String
)

@Serializable
data class Skill(
    val id: String,
    val name: String,
    val desc: List<String>,
    val abilityScore: EntityRef
)

@Serializable
data class Prerequisite(
    val id: String,
    val name: String,
    val type: String
)

@Serializable
data class SubclassSpell(
    val spell: EntityRef,
    val prerequisites: List<Prerequisite>
)

@Serializable
data class Subclass(
    val id: String,
    val name: String,
    val desc: List<String>,
    @SerialName("class")
    val clazz: EntityRef,
    val spells: List<SubclassSpell>? = null,
    val subclassFlavor: String,
    val subclassLevels: String
)

@Serializable
data class SubraceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)

@Serializable
data class Subrace(
    val id: String,
    val name: String,
    val desc: String,
    val race: EntityRef,
    val abilityBonuses: List<SubraceAbilityBonus>,
    val languages: List<EntityRef>? = null,
    val languageOptions: Choice? = null,
    val racialTraits: List<EntityRef>,
    val startingProficiencies: List<EntityRef>? = null
)

@Serializable
data class Proficiency(
    val id: String,
    val name: String
)

@Serializable
data class ActionDamage(
    val damageType: EntityRef,
    val damageAtCharacterLevel: Map<String, String>
)

@Serializable
data class Usage(
    val type: String,
    val times: Int
)

@Serializable
data class BreathWeaponAction(
    val name: String,
    val desc: String,
    val usage: Usage,
    val dc: DifficultyClass,
    val damage: List<ActionDamage>,
    val areaOfEffect: AreaOfEffect,
)

@Serializable
data class TraitSpecific(
    val subtraitOptions: Choice? = null,
    val spellOptions: Choice? = null,
    val damageType: EntityRef? = null,
    val breathWeapon: BreathWeaponAction? = null
)

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val desc: List<String>,
    val proficiencies: List<Proficiency>,
    val proficiencyChoices: Choice? = null,
    val languageOptions: Choice? = null,
    val races: List<EntityRef>,
    val subraces: List<EntityRef>,
    val parent: EntityRef? = null,
    val traitSpecific: TraitSpecific? = null
)

@Serializable
data class WeaponProperty(
    val id: String,
    val name: String,
    val desc: List<String>
)
