@file:Suppress("unused")

package com.github.arhor.spellbindr.data.newModel

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
data class Class(
    val id: String,
    val name: String,
    val classLevels: String,
    val multiClassing: MultiClassing,
    val hitDie: Int,
    val proficiencies: List<EntityRef>,
    val proficiencyChoices: List<Choice>,
    val savingThrows: List<EntityRef>,
    val spellcasting: Spellcasting? = null,
    val spells: String,
    val startingEquipment: List<EquipmentRef>,
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
    val dcType: EntityRef,
    val dcValue: Int,
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
enum class OptionSetType {
    @SerialName("equipment_category")
    EQUIPMENT_CATEGORY,

    @SerialName("resource_list")
    RESOURCE_LIST,

    @SerialName("options_array")
    OPTIONS_ARRAY
}

@Serializable
sealed class OptionSet {

    abstract val optionSetType: OptionSetType

    @Serializable
    @SerialName("equipment_category")
    data class EquipmentCategoryOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.EQUIPMENT_CATEGORY,
        @SerialName("equipment_category")
        val equipmentCategory: EntityRef
    ) : OptionSet()

    @Serializable
    @SerialName("resource_list")
    data class ResourceListOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.RESOURCE_LIST,
        @SerialName("resource_list_name")
        val resourceListName: String
    ) : OptionSet()

    @Serializable
    @SerialName("options_array")
    data class OptionsArrayOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.OPTIONS_ARRAY,
        val options: List<Option>
    ) : OptionSet()
}

@Serializable
sealed class Option {
    abstract val optionType: String

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
        val count: Int? = null,
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
    @SerialName("score_prerequisite")
    data class ScorePrerequisiteOption(
        @SerialName("option_type")
        override val optionType: String = "score_prerequisite",
        val abilityScore: EntityRef,
        val minimumScore: Int
    ) : Option()

    @Serializable
    @SerialName("ability_bonus")
    data class AbilityBonusOption(
        @SerialName("option_type")
        override val optionType: String = "ability_bonus",
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
}

@Serializable
data class CountedReferencePrerequisite(
    val type: String,
    val proficiency: EntityRef? = null
)

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
data class SpellDamage(
    val damageType: EntityRef? = null,
    val damageAtSlotLevel: Map<String, String>? = null,
    val damageAtCharacterLevel: Map<String, String>? = null,
)

@Serializable
data class SpellDC(
    val desc: String? = null,
    val dcType: EntityRef,
    val dcSuccess: String,
)

@Serializable
data class Spell(
    val areaOfEffect: AreaOfEffect? = null,
    val attackType: String? = null,
    val castingTime: String,
    val classes: List<EntityRef>,
    val components: List<String>,
    val concentration: Boolean,
    val damage: SpellDamage? = null,
    val dc: SpellDC? = null,
    val desc: List<String>,
    val duration: String,
    val healAtSlotLevel: Map<String, String>? = null,
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
