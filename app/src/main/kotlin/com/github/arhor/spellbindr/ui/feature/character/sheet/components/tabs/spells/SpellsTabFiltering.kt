package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellLevelUiModel
import java.util.Locale

internal enum class CastingTimeFilter {
    Action,
    Bonus,
    Reaction,
}

internal enum class SpellSort {
    Name,
    Level,
}

internal fun parseCastingTimeFilter(castingTime: String): CastingTimeFilter {
    val value = castingTime.lowercase(Locale.ROOT)
    return when {
        "bonus" in value -> CastingTimeFilter.Bonus
        "reaction" in value -> CastingTimeFilter.Reaction
        else -> CastingTimeFilter.Action
    }
}

internal fun filterAndSortSpellLevels(
    spellLevels: List<SpellLevelUiModel>,
    castingTime: CastingTimeFilter?,
    concentrationOnly: Boolean,
    ritualOnly: Boolean,
    sort: SpellSort,
): List<SpellLevelUiModel> {
    val comparator = when (sort) {
        SpellSort.Name -> compareBy<CharacterSpellUiModel> { it.name.lowercase(Locale.ROOT) }
        SpellSort.Level -> compareBy<CharacterSpellUiModel>({ it.level }, { it.name.lowercase(Locale.ROOT) })
    }

    val filtered = spellLevels
        .flatMap { level -> level.spells }
        .asSequence()
        .filter { spell -> castingTime == null || parseCastingTimeFilter(spell.castingTime) == castingTime }
        .filter { spell -> !concentrationOnly || spell.concentration }
        .filter { spell -> !ritualOnly || spell.ritual }
        .sortedWith(comparator)
        .toList()

    if (filtered.isEmpty()) return emptyList()

    val byLevel = filtered.groupBy { it.level }.toSortedMap()
    return byLevel.map { (level, spells) -> SpellLevelUiModel(level = level, spells = spells) }
}

