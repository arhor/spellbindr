package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection

enum class GuidedChoiceCategory {
    PROFICIENCY,
    LANGUAGE,
    ANCESTRY,
    EQUIPMENT,
}

enum class GuidedChoiceSource {
    CLASS,
    RACE_TRAIT,
    SUBRACE_TRAIT,
    BACKGROUND,
}

data class GuidedChoiceOption(
    val id: String,
    val displayName: String,
)

data class GuidedChoiceRequirement(
    val key: String,
    val source: GuidedChoiceSource,
    val sourceId: String,
    val sourceLabel: String,
    val sourceDescription: String?,
    val category: GuidedChoiceCategory,
    val choice: Choice,
    val options: List<GuidedChoiceOption>,
    val selectedOptionIds: Set<String>,
    val disabledOptions: Map<String, String>,
)

data class GuidedFixedGrant(
    val optionId: String,
    val displayName: String,
    val category: GuidedChoiceCategory,
    val source: GuidedChoiceSource,
    val sourceId: String,
    val sourceLabel: String,
)

/**
 * The reference data needed to classify and resolve guided choices.
 *
 * This deliberately depends on [GuidedSelection], not `GuidedCharacterSetupUiState.Content`, so it can be evaluated
 * while the ViewModel is still deciding which conditional steps belong in that content state.
 */
internal data class GuidedChoiceContext(
    val selection: GuidedSelection,
    val classes: List<CharacterClass> = emptyList(),
    val races: List<Race> = emptyList(),
    val backgrounds: List<Background> = emptyList(),
    val traitsById: Map<String, Trait> = emptyMap(),
    val languages: List<Language> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val featuresById: Map<String, Feature> = emptyMap(),
    val spells: List<Spell> = emptyList(),
    val referenceDataVersion: Int = 0,
)

internal data class GuidedChoiceRequirements(
    val requirements: List<GuidedChoiceRequirement>,
    val fixedGrants: List<GuidedFixedGrant>,
)

/**
 * Produces the complete, deterministically ordered set of active grants and guided choices.
 *
 * Fixed grants intentionally retain duplicate option IDs. Consumers that collapse grants for display can therefore
 * preserve every source rather than losing the explanation for a duplicate.
 */
internal fun deriveGuidedChoiceRequirements(
    context: GuidedChoiceContext,
): GuidedChoiceRequirements {
    val selection = context.selection
    val selectedClass = context.classes.firstOrNull { it.id == selection.classId }
    val selectedRace = context.races.firstOrNull { it.id == selection.raceId }
    val selectedBackground = context.backgrounds.firstOrNull { it.id == selection.backgroundId }
    val activeTraits = resolveActiveTraits(
        race = selectedRace,
        selectedSubraceId = selection.subraceId,
        traitsById = context.traitsById,
    )

    val fixedGrants = buildList {
        selectedClass?.let { clazz ->
            clazz.proficiencies.forEach { optionId ->
                add(
                    fixedGrant(
                        context = context,
                        optionId = optionId,
                        category = GuidedChoiceCategory.PROFICIENCY,
                        source = GuidedChoiceSource.CLASS,
                        sourceId = clazz.id,
                        sourceLabel = "Class: ${clazz.name}",
                    )
                )
            }
        }

        activeTraits.forEach { activeTrait ->
            addEffectGrants(
                context = context,
                effects = activeTrait.trait.effects.orEmpty(),
                source = activeTrait.source,
                sourceId = activeTrait.trait.id,
                sourceLabel = activeTrait.sourceLabel,
            )
        }

        selectedBackground?.let { background ->
            addEffectGrants(
                context = context,
                effects = background.effects,
                source = GuidedChoiceSource.BACKGROUND,
                sourceId = background.id,
                sourceLabel = "Background: ${background.name}",
            )
        }
    }.sortedWith(fixedGrantComparator)

    val drafts = buildList {
        selectedClass?.let { clazz ->
            clazz.proficiencyChoices.forEachIndexed { index, choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index),
                        source = GuidedChoiceSource.CLASS,
                        sourceId = clazz.id,
                        sourceLabel = "Class: ${clazz.name}",
                        sourceDescription = null,
                        category = GuidedChoiceCategory.PROFICIENCY,
                        choice = choice,
                    )
                )
            }
        }

        activeTraits.forEach { activeTrait ->
            val trait = activeTrait.trait
            val description = trait.desc.takeIf { it.isNotEmpty() }?.joinToString("\n\n")

            trait.proficiencyChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id),
                        source = activeTrait.source,
                        sourceId = trait.id,
                        sourceLabel = activeTrait.sourceLabel,
                        sourceDescription = description,
                        category = GuidedChoiceCategory.PROFICIENCY,
                        choice = choice,
                    )
                )
            }
            trait.languageChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id),
                        source = activeTrait.source,
                        sourceId = trait.id,
                        sourceLabel = activeTrait.sourceLabel,
                        sourceDescription = description,
                        category = GuidedChoiceCategory.LANGUAGE,
                        choice = choice,
                    )
                )
            }
            trait.abilityBonusChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id),
                        source = activeTrait.source,
                        sourceId = trait.id,
                        sourceLabel = activeTrait.sourceLabel,
                        sourceDescription = description,
                        category = GuidedChoiceCategory.ANCESTRY,
                        choice = choice,
                    )
                )
            }
            trait.draconicAncestryChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id),
                        source = activeTrait.source,
                        sourceId = trait.id,
                        sourceLabel = activeTrait.sourceLabel,
                        sourceDescription = description,
                        category = GuidedChoiceCategory.ANCESTRY,
                        choice = choice,
                    )
                )
            }
            trait.spellChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id),
                        source = activeTrait.source,
                        sourceId = trait.id,
                        sourceLabel = activeTrait.sourceLabel,
                        sourceDescription = description,
                        category = GuidedChoiceCategory.ANCESTRY,
                        choice = choice,
                    )
                )
            }
        }

        selectedBackground?.let { background ->
            background.languageChoice?.let { choice ->
                add(
                    requirementDraft(
                        context = context,
                        key = GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey(),
                        source = GuidedChoiceSource.BACKGROUND,
                        sourceId = background.id,
                        sourceLabel = "Background: ${background.name}",
                        sourceDescription = null,
                        category = GuidedChoiceCategory.LANGUAGE,
                        choice = choice,
                    )
                )
            }
            background.equipmentChoice?.let { choice ->
                expandNestedChoices(
                    baseKey = GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey(),
                    choice = choice,
                ).forEach { (key, expandedChoice) ->
                    add(
                        requirementDraft(
                            context = context,
                            key = key,
                            source = GuidedChoiceSource.BACKGROUND,
                            sourceId = background.id,
                            sourceLabel = "Background: ${background.name}",
                            sourceDescription = null,
                            category = GuidedChoiceCategory.EQUIPMENT,
                            choice = expandedChoice,
                        )
                    )
                }
            }
        }
    }.sortedWith(requirementDraftComparator)

    val requirements = drafts.map { draft ->
        draft.toRequirement(
            disabledOptions = duplicateReasonsFor(
                current = draft,
                requirements = drafts,
                fixedGrants = fixedGrants,
            )
        )
    }

    return GuidedChoiceRequirements(
        requirements = requirements,
        fixedGrants = fixedGrants,
    )
}

internal fun backgroundNestedEquipmentChoiceKey(index: Int): String =
    "${GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()}/$index"

private fun expandNestedChoices(
    baseKey: String,
    choice: Choice,
): List<Pair<String, Choice>> = if (choice is Choice.NestedChoice) {
    choice.from.flatMapIndexed { index, nestedChoice ->
        expandNestedChoices("$baseKey/$index", nestedChoice)
    }
} else {
    listOf(baseKey to choice)
}

private data class ActiveTrait(
    val trait: Trait,
    val source: GuidedChoiceSource,
) {
    val sourceLabel: String
        get() = when (source) {
            GuidedChoiceSource.RACE_TRAIT -> "Race trait: ${trait.name}"
            GuidedChoiceSource.SUBRACE_TRAIT -> "Subrace trait: ${trait.name}"
            else -> trait.name
        }
}

private data class RequirementDraft(
    val key: String,
    val source: GuidedChoiceSource,
    val sourceId: String,
    val sourceLabel: String,
    val sourceDescription: String?,
    val category: GuidedChoiceCategory,
    val choice: Choice,
    val options: List<GuidedChoiceOption>,
    val selectedOptionIds: Set<String>,
) {
    fun toRequirement(disabledOptions: Map<String, String>) = GuidedChoiceRequirement(
        key = key,
        source = source,
        sourceId = sourceId,
        sourceLabel = sourceLabel,
        sourceDescription = sourceDescription,
        category = category,
        choice = choice,
        options = options,
        selectedOptionIds = selectedOptionIds,
        disabledOptions = disabledOptions,
    )
}

private fun resolveActiveTraits(
    race: Race?,
    selectedSubraceId: String?,
    traitsById: Map<String, Trait>,
): List<ActiveTrait> {
    if (race == null) return emptyList()

    val baseTraits = race.traits.mapNotNull { reference ->
        traitsById[reference.id]?.let { ActiveTrait(it, GuidedChoiceSource.RACE_TRAIT) }
    }
    val subraceTraits = race.subraces
        .firstOrNull { it.id == selectedSubraceId }
        ?.traits
        .orEmpty()
        .mapNotNull { reference ->
            traitsById[reference.id]?.let { ActiveTrait(it, GuidedChoiceSource.SUBRACE_TRAIT) }
        }

    return (baseTraits + subraceTraits).distinctBy { it.trait.id }
}

private fun requirementDraft(
    context: GuidedChoiceContext,
    key: String,
    source: GuidedChoiceSource,
    sourceId: String,
    sourceLabel: String,
    sourceDescription: String?,
    category: GuidedChoiceCategory,
    choice: Choice,
) = RequirementDraft(
    key = key,
    source = source,
    sourceId = sourceId,
    sourceLabel = sourceLabel,
    sourceDescription = sourceDescription,
    category = category,
    choice = choice,
    options = resolveOptions(choice, category, context),
    selectedOptionIds = context.selection.choiceSelections[key].orEmpty(),
)

private fun MutableList<GuidedFixedGrant>.addEffectGrants(
    context: GuidedChoiceContext,
    effects: List<Effect>,
    source: GuidedChoiceSource,
    sourceId: String,
    sourceLabel: String,
) {
    effects.forEach { effect ->
        when (effect) {
            is Effect.AddProficienciesEffect -> effect.proficiencies.forEach { optionId ->
                add(
                    fixedGrant(
                        context = context,
                        optionId = optionId,
                        category = GuidedChoiceCategory.PROFICIENCY,
                        source = source,
                        sourceId = sourceId,
                        sourceLabel = sourceLabel,
                    )
                )
            }

            is Effect.AddLanguagesEffect -> effect.languages.forEach { optionId ->
                add(
                    fixedGrant(
                        context = context,
                        optionId = optionId,
                        category = GuidedChoiceCategory.LANGUAGE,
                        source = source,
                        sourceId = sourceId,
                        sourceLabel = sourceLabel,
                    )
                )
            }

            else -> Unit
        }
    }
}

private fun fixedGrant(
    context: GuidedChoiceContext,
    optionId: String,
    category: GuidedChoiceCategory,
    source: GuidedChoiceSource,
    sourceId: String,
    sourceLabel: String,
) = GuidedFixedGrant(
    optionId = optionId,
    displayName = displayNameForOption(optionId, context),
    category = category,
    source = source,
    sourceId = sourceId,
    sourceLabel = sourceLabel,
)

private fun duplicateReasonsFor(
    current: RequirementDraft,
    requirements: List<RequirementDraft>,
    fixedGrants: List<GuidedFixedGrant>,
): Map<String, String> {
    if (current.category != GuidedChoiceCategory.PROFICIENCY &&
        current.category != GuidedChoiceCategory.LANGUAGE
    ) {
        return emptyMap()
    }

    val fixedSourcesByOption = fixedGrants
        .asSequence()
        .filter { it.category == current.category }
        .groupBy(keySelector = { it.optionId }, valueTransform = { it.sourceLabel })
    val choiceSourcesByOption = requirements
        .asSequence()
        .filter { it.key != current.key && it.category == current.category }
        .flatMap { requirement ->
            requirement.selectedOptionIds.asSequence().map { optionId -> optionId to requirement.sourceLabel }
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    return current.options.mapNotNull { option ->
        val fixedSources = fixedSourcesByOption[option.id].orEmpty().distinct()
        val choiceSources = choiceSourcesByOption[option.id].orEmpty().distinct()
        val reason = buildList {
            if (fixedSources.isNotEmpty()) {
                add("Granted by ${fixedSources.joinToString()}")
            }
            if (choiceSources.isNotEmpty()) {
                add("Selected for ${choiceSources.joinToString()}")
            }
        }.joinToString("; ")

        reason.takeIf { it.isNotEmpty() }?.let { option.id to it }
    }.toMap(linkedMapOf())
}

private fun resolveOptions(
    choice: Choice,
    category: GuidedChoiceCategory,
    context: GuidedChoiceContext,
): List<GuidedChoiceOption> = when (choice) {
    is Choice.FromAllChoice -> when (category) {
        GuidedChoiceCategory.LANGUAGE -> context.languages
            .sortedBy { it.name }
            .map { GuidedChoiceOption(it.id, it.name) }

        else -> emptyList()
    }

    is Choice.OptionsArrayChoice -> choice.from.distinct().map { optionId ->
        GuidedChoiceOption(optionId, displayNameForOption(optionId, context))
    }

    is Choice.ProficiencyChoice -> choice.from.distinct().map { optionId ->
        GuidedChoiceOption(optionId, displayNameForOption(optionId, context))
    }

    is Choice.EquipmentChoice -> choice.from.distinct().map { optionId ->
        GuidedChoiceOption(optionId, displayNameForOption(optionId, context))
    }

    is Choice.EquipmentCategoriesChoice -> {
        val requiredCategories = choice.from.categories.mapNotNull(::equipmentCategoryFromId).toSet()
        context.equipment
            .asSequence()
            .filter { item -> requiredCategories.all { it in item.categories } }
            .sortedBy { it.name }
            .map { GuidedChoiceOption(it.id, it.name) }
            .toList()
    }

    is Choice.FeatureChoice -> choice.from.distinct().map { optionId ->
        GuidedChoiceOption(optionId, displayNameForOption(optionId, context))
    }

    is Choice.FavoredEnemyChoice -> choice.from.distinct().map { GuidedChoiceOption(it, it) }

    is Choice.TerrainTypeChoice -> choice.from.distinct().map { GuidedChoiceOption(it, it) }

    is Choice.AbilityBonusChoice -> choice.from
        .flatMap { bonus -> bonus.entries }
        .distinctBy { it.key }
        .map { (abilityId, bonus) ->
            GuidedChoiceOption(abilityId, "${abilityId.displayName()} ${bonus.asSignedBonus()}")
        }

    is Choice.ResourceListChoice -> when (choice.from.lowercase()) {
        "languages" -> context.languages
            .sortedBy { it.name }
            .map { GuidedChoiceOption(it.id, it.name) }

        "spells" -> {
            var spells = context.spells.asSequence()
            choice.where.orEmpty().forEach { (key, value) ->
                when (key.lowercase()) {
                    "classes" -> spells = spells.filter { spell -> spell.classes.any { it.id == value } }
                    "level" -> value.toIntOrNull()?.let { level ->
                        spells = spells.filter { it.level == level }
                    }
                }
            }
            spells.sortedBy { it.name }.map { GuidedChoiceOption(it.id, it.name) }.toList()
        }

        else -> emptyList()
    }

    // A nested choice has independent child limits and must be expanded into separate requirements by its owner.
    is Choice.NestedChoice -> emptyList()

    is Choice.IdealChoice -> emptyList()
}

private fun displayNameForOption(
    id: String,
    context: GuidedChoiceContext,
): String {
    skillDisplayName(id)?.let { return it }
    context.languages.firstOrNull { it.id == id }?.let { return it.name }
    context.equipment.firstOrNull { it.id == id }?.let { return it.name }
    context.featuresById[id]?.let { return it.name }
    context.traitsById[id]?.let { return it.name }
    context.spells.firstOrNull { it.id == id }?.let { return it.name }
    return EntityRef(id).prettyString()
}

private fun skillDisplayName(id: String): String? {
    if (!id.startsWith("skill-")) return null
    val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
    return Skill.entries.firstOrNull { it.name == normalized }?.displayName
}

private fun Int.asSignedBonus(): String = if (this >= 0) "+$this" else toString()

private val requirementDraftComparator =
    compareBy<RequirementDraft>({ it.category.ordinal }, { it.source.ordinal }, { it.sourceLabel }, { it.key })

private val fixedGrantComparator =
    compareBy<GuidedFixedGrant>(
        { it.category.ordinal },
        { it.source.ordinal },
        { it.sourceLabel },
        { it.displayName },
        { it.optionId },
    )

private fun equipmentCategoryFromId(id: String): EquipmentCategory? = when (id) {
    "weapon" -> EquipmentCategory.WEAPON
    "armor" -> EquipmentCategory.ARMOR
    "tool" -> EquipmentCategory.TOOL
    "gear" -> EquipmentCategory.GEAR
    "holy-symbol" -> EquipmentCategory.HOLY_SYMBOL
    "standard" -> EquipmentCategory.STANDARD
    "musical-instrument" -> EquipmentCategory.MUSICAL_INSTRUMENT
    "gaming-set" -> EquipmentCategory.GAMING_SET
    "other" -> EquipmentCategory.OTHER
    "arcane-focus" -> EquipmentCategory.ARCANE_FOCUS
    "druidic-focus" -> EquipmentCategory.DRUIDIC_FOCUS
    "kit" -> EquipmentCategory.KIT
    "simple" -> EquipmentCategory.SIMPLE
    "martial" -> EquipmentCategory.MARTIAL
    "ranged" -> EquipmentCategory.RANGED
    "melee" -> EquipmentCategory.MELEE
    "shield" -> EquipmentCategory.SHIELD
    "light" -> EquipmentCategory.LIGHT
    "heavy" -> EquipmentCategory.HEAVY
    "medium" -> EquipmentCategory.MEDIUM
    "ammunition" -> EquipmentCategory.AMMUNITION
    "equipment-pack" -> EquipmentCategory.EQUIPMENT_PACK
    "artisans-tool" -> EquipmentCategory.ARTISANS_TOOL
    "gaming-sets" -> EquipmentCategory.GAMING_SETS
    "mounts-and-other-animals" -> EquipmentCategory.MOUNTS_AND_OTHER_ANIMALS
    "vehicle" -> EquipmentCategory.VEHICLE
    "tack-harness-and-drawn-vehicle" -> EquipmentCategory.TACK_HARNESS_AND_DRAWN_VEHICLE
    "waterborne-vehicle" -> EquipmentCategory.WATERBORNE_VEHICLE
    else -> null
}
