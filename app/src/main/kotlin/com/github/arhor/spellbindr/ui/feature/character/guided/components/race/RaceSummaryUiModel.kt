package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.model.displayName

@Immutable
internal data class RaceSummaryUiModel(
    val raceId: String,
    val name: String,
    val selectedSubraceName: String?,
    val selectedSubraceDescription: String?,
    val size: String?,
    val speedFeet: Int?,
    val abilityBonuses: List<String>,
    val definingTraits: List<String>,
    val deferredChoices: List<DeferredRaceChoiceUiModel>,
    val traitDetails: List<RaceTraitDetailUiModel>,
) {
    fun conciseDescription(): String = buildList {
        size?.let { add("Size $it") }
        speedFeet?.let { add("Speed $it feet") }
        if (abilityBonuses.isNotEmpty()) add("Ability bonuses ${abilityBonuses.joinToString()}")
        if (definingTraits.isNotEmpty()) add("Traits ${definingTraits.joinToString()}")
        if (deferredChoices.isNotEmpty()) add(deferredChoices.joinToString { it.label })
    }.joinToString(". ")
}

@Immutable
internal data class DeferredRaceChoiceUiModel(
    val count: Int,
    val category: String,
) {
    val label: String
        get() = "$count $category ${if (count == 1) "choice" else "choices"} later"
}

@Immutable
internal data class RaceTraitDetailUiModel(
    val id: String,
    val name: String,
    val descriptions: List<String>,
)

internal fun raceSummaryUiModel(
    race: Race,
    traitsById: Map<String, Trait>,
    selectedSubraceId: String?,
): RaceSummaryUiModel {
    val selectedSubrace = race.subraces.firstOrNull { it.id == selectedSubraceId }
    val traits = buildList {
        race.traits.mapNotNullTo(this) { traitsById[it.id] }
        selectedSubrace?.traits?.mapNotNullTo(this) { traitsById[it.id] }
    }
    val effects = traits.flatMap { it.effects.orEmpty() }
    val abilityBonusTotals = effects
        .filterIsInstance<Effect.ModifyAbilityEffect>()
        .flatMap { it.abilities.entries }
        .groupingBy { it.key.lowercase() }
        .fold(0) { total, entry -> total + entry.value }
        .filterValues { it != 0 }
    val abilityBonuses = if (
        abilityBonusTotals.keys.containsAll(AbilityIds.standardOrder) &&
        AbilityIds.standardOrder.mapNotNull(abilityBonusTotals::get).distinct().size == 1
    ) {
        val bonus = abilityBonusTotals.getValue(AbilityIds.standardOrder.first())
        listOf("All abilities ${if (bonus > 0) "+" else ""}$bonus")
    } else {
        abilityBonusTotals
            .toList()
            .sortedWith(compareBy({ abilityOrder(it.first) }, { it.first }))
            .map { (abilityId, bonus) ->
                "${abilityId.displayName()} ${if (bonus > 0) "+" else ""}$bonus"
            }
    }

    val definingTraits = traits
        .filterNot(::isGenericRaceTrait)
        .map { it.name }
        .distinct()
        .take(MAX_DEFINING_TRAITS)
        .ifEmpty { traits.map { it.name }.distinct().take(MAX_DEFINING_TRAITS) }

    return RaceSummaryUiModel(
        raceId = race.id,
        name = race.name,
        selectedSubraceName = selectedSubrace?.name,
        selectedSubraceDescription = selectedSubrace?.desc,
        size = effects.filterIsInstance<Effect.ModifySizeEffect>().lastOrNull()?.size?.replaceFirstChar {
            it.uppercase()
        },
        speedFeet = effects.filterIsInstance<Effect.ModifySpeedEffect>().lastOrNull()?.speed,
        abilityBonuses = abilityBonuses,
        definingTraits = definingTraits,
        deferredChoices = deferredChoices(traits),
        traitDetails = traits.map { trait ->
            RaceTraitDetailUiModel(
                id = trait.id,
                name = trait.name,
                descriptions = trait.desc,
            )
        },
    )
}

private fun deferredChoices(traits: List<Trait>): List<DeferredRaceChoiceUiModel> = buildList {
    traits.sumOf { it.proficiencyChoice?.choose ?: 0 }
        .takeIf { it > 0 }
        ?.let { add(DeferredRaceChoiceUiModel(it, "proficiency")) }
    traits.sumOf { it.languageChoice?.choose ?: 0 }
        .takeIf { it > 0 }
        ?.let { add(DeferredRaceChoiceUiModel(it, "language")) }
    traits.sumOf { trait ->
        listOf(
            trait.abilityBonusChoice,
            trait.draconicAncestryChoice,
            trait.spellChoice,
        ).sumOf { it?.choose ?: 0 }
    }.takeIf { it > 0 }
        ?.let { add(DeferredRaceChoiceUiModel(it, "ancestry")) }
}

private fun abilityOrder(abilityId: String): Int =
    AbilityIds.standardOrder.indexOf(abilityId).takeIf { it >= 0 } ?: Int.MAX_VALUE

private fun isGenericRaceTrait(trait: Trait): Boolean =
    trait.name.lowercase() in GENERIC_TRAIT_NAMES ||
        trait.id.substringBeforeLast('-', missingDelimiterValue = trait.id) in GENERIC_TRAIT_IDS

private const val MAX_DEFINING_TRAITS = 3

private val GENERIC_TRAIT_NAMES = setOf(
    "ability score increase",
    "age",
    "alignment",
    "languages",
    "size",
    "speed",
)

private val GENERIC_TRAIT_IDS = setOf(
    "ability-score-increase",
    "age",
    "alignment",
    "languages",
    "size",
    "speed",
)
