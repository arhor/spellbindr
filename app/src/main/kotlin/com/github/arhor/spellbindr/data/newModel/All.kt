@file:Suppress("unused")

package com.github.arhor.spellbindr.data.newModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AbilityScore(
    val id: String,
    val name: String,
    val desc: List<String>,
    @SerialName("full_name")
    val fullName: String,
    val skills: List<EntityRef>
)

@Serializable
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbreviation: String
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
    @SerialName("starting_proficiencies")
    val startingProficiencies: List<EntityRef>,
    @SerialName("language_options")
    val languageOptions: Choice,
    @SerialName("starting_equipment")
    val startingEquipment: List<EquipmentRef>,
    @SerialName("starting_equipment_options")
    val startingEquipmentOptions: List<Choice>,
    val feature: GenericInfo,
    @SerialName("personality_traits")
    val personalityTraits: Choice,
    val ideals: Choice,
    val bonds: Choice,
    val flaws: Choice
)

@Serializable
data class Spellcasting(
    val info: List<GenericInfo>,
    val level: Int,
    @SerialName("spellcasting_ability")
    val spellcastingAbility: EntityRef
)

@Serializable
data class MultiClassingPrereq(
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    @SerialName("minimum_score")
    val minimumScore: Int
)

@Serializable
data class MultiClassing(
    val prerequisites: List<MultiClassingPrereq>? = null,
    @SerialName("prerequisite_options")
    val prerequisiteOptions: Choice? = null,
    val proficiencies: List<EntityRef>? = null,
    @SerialName("proficiency_choices")
    val proficiencyChoices: List<Choice>? = null
)

@Serializable
data class Class(
    val id: String,
    val name: String,
    @SerialName("class_levels")
    val classLevels: String,
    @SerialName("multi_classing")
    val multiClassing: MultiClassing,
    @SerialName("hit_die")
    val hitDie: Int,
    val proficiencies: List<EntityRef>,
    @SerialName("proficiency_choices")
    val proficiencyChoices: List<Choice>,
    @SerialName("saving_throws")
    val savingThrows: List<EntityRef>,
    val spellcasting: Spellcasting? = null,
    val spells: String,
    @SerialName("starting_equipment")
    val startingEquipment: List<EquipmentRef>,
    @SerialName("starting_equipment_options")
    val startingEquipmentOptions: List<Choice>,
    val subclasses: List<EntityRef>
)

@Serializable
data class EntityRef(
    val id: String,
)

@Serializable
data class AreaOfEffect(
    val size: Int,
    val type: AreaOfEffectType
)

@Serializable
enum class AreaOfEffectType {
    @SerialName("sphere")
    SPHERE,
    @SerialName("cube")
    CUBE,
    @SerialName("cylinder")
    CYLINDER,
    @SerialName("line")
    LINE,
    @SerialName("cone")
    CONE
}

@Serializable
data class DifficultyClass(
    @SerialName("dc_type")
    val dcType: EntityRef,
    @SerialName("dc_value")
    val dcValue: Int,
    @SerialName("success_type")
    val successType: SuccessType
)

@Serializable
enum class SuccessType {
    @SerialName("none")
    NONE,
    @SerialName("half")
    HALF,
    @SerialName("other")
    OTHER
}

@Serializable
data class Damage(
    val damageDice: String,
    val damageType: EntityRef
)

@Serializable
sealed class OptionSet {
    abstract val optionSetType: OptionSetType
}

@Serializable
enum class OptionSetType {
    @SerialName("equipment_category")
    EQUIPMENT_CATEGORY,
    @SerialName("resource_list")
    RESOURCE_LIST,
    @SerialName("options_array")
    OPTIONS_ARRAY
}

@Serializable
@SerialName("equipment_category")
data class EquipmentCategoryOptionSet(
    @SerialName("option_set_type")
    override val optionSetType: OptionSetType = OptionSetType.EQUIPMENT_CATEGORY,
    @SerialName("equipment_category")
    val equipmentCategory: EntityRef
) : OptionSet()

@Serializable
@SerialName("resource_list")
data class ResourceListOptionSet(
    @SerialName("option_set_type")
    override val optionSetType: OptionSetType = OptionSetType.RESOURCE_LIST,
    @SerialName("resource_list_name")
    val resourceListName: String
) : OptionSet()

@Serializable
@SerialName("options_array")
data class OptionsArrayOptionSet(
    @SerialName("option_set_type")
    override val optionSetType: OptionSetType = OptionSetType.OPTIONS_ARRAY,
    val options: List<Option>
) : OptionSet()

@Serializable
sealed class Option {
    abstract val optionType: String
}

@Serializable
open class BaseOption(
    @SerialName("option_type")
    override val optionType: String
) : Option()

@Serializable
@SerialName("reference")
data class ReferenceOption(
    @SerialName("option_type")
    override val optionType: String = "reference",
    val item: EntityRef
) : Option()

@Serializable
@SerialName("action")
data class ActionOption(
    @SerialName("option_type")
    override val optionType: String = "action",
    @SerialName("action_name")
    val actionName: String,
    val count: Int? = null, // Может быть строкой, если нужно - поменяй на Any и кастуй вручную
    val type: String,
    val notes: String? = null
) : Option()

@Serializable
@SerialName("multiple")
data class MultipleOption(
    @SerialName("option_type")
    override val optionType: String = "multiple",
    val items: List<Option>
) : Option()

@Serializable
@SerialName("string")
data class StringOption(
    @SerialName("option_type")
    override val optionType: String = "string",
    val string: String
) : Option()

@Serializable
@SerialName("ideal")
data class IdealOption(
    @SerialName("option_type")
    override val optionType: String = "ideal",
    val desc: String,
    val alignments: List<EntityRef>
) : Option()

@Serializable
@SerialName("counted_reference")
data class CountedReferenceOption(
    @SerialName("option_type")
    override val optionType: String = "counted_reference",
    val count: Int,
    val of: EntityRef,
    val prerequisites: List<CountedReferencePrerequisite>? = null
) : Option()

@Serializable
data class CountedReferencePrerequisite(
    val type: String, // "proficiency"
    val proficiency: EntityRef? = null
)

@Serializable
@SerialName("score_prerequisite")
data class ScorePrerequisiteOption(
    @SerialName("option_type")
    override val optionType: String = "score_prerequisite",
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    @SerialName("minimum_score")
    val minimumScore: Int
) : Option()

@Serializable
@SerialName("ability_bonus")
data class AbilityBonusOption(
    @SerialName("option_type")
    override val optionType: String = "ability_bonus",
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    val bonus: Int
) : Option()

@Serializable
@SerialName("breath")
data class BreathOption(
    @SerialName("option_type")
    override val optionType: String = "breath",
    val name: String,
    val dc: DifficultyClass,
    val damage: List<Damage>? = null
) : Option()

@Serializable
@SerialName("damage")
data class DamageOption(
    @SerialName("option_type")
    override val optionType: String = "damage",
    @SerialName("damage_type")
    val damageType: EntityRef,
    @SerialName("damage_dice")
    val damageDice: String,
    val notes: String? = null
) : Option()

@Serializable
@SerialName("choice")
data class ChoiceOption(
    @SerialName("option_type")
    override val optionType: String = "choice",
    val choice: Choice
) : Option()

@Serializable
data class Choice(
    val desc: String,
    val choose: Int,
    val type: String,
    val from: OptionSet
)

@Serializable
data class Condition(
    val id: String,
    val name: String,
    val desc: List<String>
)

@Serializable
data class DamageType(
    val id: String,
    val name: String,
    val desc: List<String>
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
    @SerialName("damage_type")
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
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    @SerialName("minimum_score")
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
sealed class CommonPrerequisite

@Serializable
data class LevelPrerequisiteWrapper(val level: LevelPrerequisite) : CommonPrerequisite()

@Serializable
data class FeaturePrerequisiteWrapper(val feature: FeaturePrerequisite) : CommonPrerequisite()

@Serializable
data class SpellPrerequisiteWrapper(val spell: SpellPrerequisite) : CommonPrerequisite()

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
    val count: String, // TS: number | string
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
}

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
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    val bonus: Int
)

@Serializable
data class Race(
    @SerialName("ability_bonus_options")
    val abilityBonusOptions: Choice? = null,
    @SerialName("ability_bonuses")
    val abilityBonuses: List<RaceAbilityBonus>,
    val age: String,
    val alignment: String,
    val id: String,
    @SerialName("language_desc")
    val languageDesc: String,
    @SerialName("language_options")
    val languageOptions: Choice,
    val languages: List<EntityRef>,
    val name: String,
    val size: String,
    @SerialName("size_description")
    val sizeDescription: String,
    val speed: Int,
    @SerialName("starting_proficiencies")
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
    @SerialName("ability_score")
    val abilityScore: EntityRef
)

@Serializable
data class SpellDamage(
    @SerialName("damage_type")
    val damageType: EntityRef? = null,
    @SerialName("damage_at_slot_level")
    val damageAtSlotLevel: Map<String, String>? = null,
    @SerialName("damage_at_character_level")
    val damageAtCharacterLevel: Map<String, String>? = null
)

@Serializable
data class SpellDC(
    val desc: String? = null,
    @SerialName("dc_type")
    val dcType: EntityRef,
    @SerialName("dc_success")
    val dcSuccess: String
)

@Serializable
data class Spell(
    @SerialName("area_of_effect")
    val areaOfEffect: AreaOfEffect? = null,
    @SerialName("attack_type")
    val attackType: String? = null,
    @SerialName("casting_time")
    val castingTime: String,
    val classes: List<EntityRef>,
    val components: List<String>,
    val concentration: Boolean,
    val damage: SpellDamage? = null,
    val dc: SpellDC? = null,
    val desc: List<String>,
    val duration: String,
    @SerialName("heal_at_slot_level")
    val healAtSlotLevel: Map<String, String>? = null,
    @SerialName("higher_level")
    val higherLevel: List<String>? = null,
    val id: String,
    val level: Int,
    val material: String? = null,
    val name: String,
    val range: String,
    val ritual: Boolean,
    val school: EntityRef,
    val subclasses: List<EntityRef>? = null
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
    @SerialName("subclass_flavor")
    val subclassFlavor: String,
    @SerialName("subclass_levels")
    val subclassLevels: String
)

@Serializable
data class SubraceAbilityBonus(
    @SerialName("ability_score")
    val abilityScore: EntityRef,
    val bonus: Int
)

@Serializable
data class Subrace(
    val id: String,
    val name: String,
    val desc: String,
    val race: EntityRef,
    @SerialName("ability_bonuses")
    val abilityBonuses: List<SubraceAbilityBonus>,
    val languages: List<EntityRef>? = null,
    @SerialName("language_options")
    val languageOptions: Choice? = null,
    @SerialName("racial_traits")
    val racialTraits: List<EntityRef>,
    @SerialName("starting_proficiencies")
    val startingProficiencies: List<EntityRef>? = null
)

@Serializable
data class Proficiency(
    val id: String,
    val name: String
)

@Serializable
data class ActionDamage(
    @SerialName("damage_type")
    val damageType: EntityRef,
    @SerialName("damage_at_character_level")
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
    @SerialName("area_of_effect")
    val areaOfEffect: AreaOfEffect
)

@Serializable
data class TraitSpecific(
    @SerialName("subtrait_options")
    val subtraitOptions: Choice? = null,
    @SerialName("spell_options")
    val spellOptions: Choice? = null,
    @SerialName("damage_type")
    val damageType: EntityRef? = null,
    @SerialName("breath_weapon")
    val breathWeapon: BreathWeaponAction? = null
)

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val desc: List<String>,
    val proficiencies: List<Proficiency>,
    @SerialName("proficiency_choices")
    val proficiencyChoices: Choice? = null,
    @SerialName("language_options")
    val languageOptions: Choice? = null,
    val races: List<EntityRef>,
    val subraces: List<EntityRef>,
    val parent: EntityRef? = null,
    @SerialName("trait_specific")
    val traitSpecific: TraitSpecific? = null
)

@Serializable
data class WeaponProperty(
    val id: String,
    val name: String,
    val desc: List<String>
)

fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }
    val data = json.decodeFromString<List<WeaponProperty>>(
        """
            [
              {
                "id": "ammunition",
                "name": "Ammunition",
                "desc": [
                  "You can use a weapon that has the ammunition property to make a ranged attack only if you have ammunition to fire from the weapon. Each time you attack with the weapon, you expend one piece of ammunition. Drawing the ammunition from a quiver, case, or other container is part of the attack (you need a free hand to load a one-handed weapon).",
                  "At the end of the battle, you can recover half your expended ammunition by taking a minute to search the battlefield. If you use a weapon that has the ammunition property to make a melee attack, you treat the weapon as an improvised weapon (see \"Improvised Weapons\" later in the section). A sling must be loaded to deal any damage when used in this way."
                ],
                "url": "/api/2014/weapon-properties/ammunition"
              },
              {
                "id": "finesse",
                "name": "Finesse",
                "desc": [
                  "When making an attack with a finesse weapon, you use your choice of your Strength or Dexterity modifier for the attack and damage rolls. You must use the same modifier for both rolls."
                ],
                "url": "/api/2014/weapon-properties/finesse"
              },
              {
                "id": "heavy",
                "name": "Heavy",
                "desc": [
                  "Small creatures have disadvantage on attack rolls with heavy weapons. A heavy weapon's size and bulk make it too large for a Small creature to use effectively."
                ],
                "url": "/api/2014/weapon-properties/heavy"
              },
              {
                "id": "light",
                "name": "Light",
                "desc": [
                  "A light weapon is small and easy to handle, making it ideal for use when fighting with two weapons."
                ],
                "url": "/api/2014/weapon-properties/light"
              },
              {
                "id": "loading",
                "name": "Loading",
                "desc": [
                  "Because of the time required to load this weapon, you can fire only one piece of ammunition from it when you use an action, bonus action, or reaction to fire it, regardless of the number of attacks you can normally make."
                ],
                "url": "/api/2014/weapon-properties/loading"
              },
              {
                "id": "reach",
                "name": "Reach",
                "desc": [
                  "This weapon adds 5 feet to your reach when you attack with it, as well as when determining your reach for opportunity attacks with it."
                ],
                "url": "/api/2014/weapon-properties/reach"
              },
              {
                "id": "special",
                "name": "Special",
                "desc": [
                  "A weapon with the special property has unusual rules governing its use, explained in the weapon's description (see \"Special Weapons\" later in this section)."
                ],
                "url": "/api/2014/weapon-properties/special"
              },
              {
                "id": "thrown",
                "name": "Thrown",
                "desc": [
                  "If a weapon has the thrown property, you can throw the weapon to make a ranged attack. If the weapon is a melee weapon, you use the same ability modifier for that attack roll and damage roll that you would use for a melee attack with the weapon. For example, if you throw a handaxe, you use your Strength, but if you throw a dagger, you can use either your Strength or your Dexterity, since the dagger has the finesse property."
                ],
                "url": "/api/2014/weapon-properties/thrown"
              },
              {
                "id": "two-handed",
                "name": "Two-Handed",
                "desc": [
                  "This weapon requires two hands when you attack with it."
                ],
                "url": "/api/2014/weapon-properties/two-handed"
              },
              {
                "id": "versatile",
                "name": "Versatile",
                "desc": [
                  "This weapon can be used with one or two hands. A damage value in parentheses appears with the property--the damage when the weapon is used with two hands to make a melee attack."
                ],
                "url": "/api/2014/weapon-properties/versatile"
              },
              {
                "id": "monk",
                "name": "Monk",
                "desc": [
                  "Monks gain several benefits while unarmed or wielding only monk weapons while they aren't wearing armor or wielding shields."
                ],
                "url": "/api/2014/weapon-properties/monk"
              }
            ]
        """.trimIndent()
    )

    data.forEach { println(it) }
}
