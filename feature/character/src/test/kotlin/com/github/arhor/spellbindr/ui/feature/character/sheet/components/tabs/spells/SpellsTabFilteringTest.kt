package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellLevelUiModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpellsTabFilteringTest {

    @Test
    fun `parseCastingTimeFilter maps action`() {
        assertThat(parseCastingTimeFilter("Action")).isEqualTo(CastingTimeFilter.Action)
        assertThat(parseCastingTimeFilter("1 action")).isEqualTo(CastingTimeFilter.Action)
        assertThat(parseCastingTimeFilter("Cast")).isEqualTo(CastingTimeFilter.Action)
    }

    @Test
    fun `parseCastingTimeFilter maps bonus and reaction`() {
        assertThat(parseCastingTimeFilter("Bonus Action")).isEqualTo(CastingTimeFilter.Bonus)
        assertThat(parseCastingTimeFilter("1 bonus action")).isEqualTo(CastingTimeFilter.Bonus)
        assertThat(parseCastingTimeFilter("Reaction")).isEqualTo(CastingTimeFilter.Reaction)
    }

    @Test
    fun `filterAndSortSpellLevels filters by casting time and tags`() {
        val spells = listOf(
            spell(name = "A", level = 0, castingTime = "Action", concentration = false, ritual = false),
            spell(name = "B", level = 1, castingTime = "Bonus Action", concentration = true, ritual = false),
            spell(name = "C", level = 1, castingTime = "Reaction", concentration = false, ritual = true),
        )
        val levels = listOf(
            SpellLevelUiModel(level = 0, spells = spells.filter { it.level == 0 }),
            SpellLevelUiModel(level = 1, spells = spells.filter { it.level == 1 }),
        )

        val result = filterAndSortSpellLevels(
            spellLevels = levels,
            castingTime = CastingTimeFilter.Reaction,
            concentrationOnly = false,
            ritualOnly = true,
            sort = SpellSort.Level,
        )

        assertThat(result).hasSize(1)
        assertThat(result.single().level).isEqualTo(1)
        assertThat(result.single().spells.map { it.name }).containsExactly("C")
    }

    @Test
    fun `filterAndSortSpellLevels sorts by level then name`() {
        val spells = listOf(
            spell(name = "Zeta", level = 1, castingTime = "Action", concentration = false, ritual = false),
            spell(name = "Alpha", level = 0, castingTime = "Action", concentration = false, ritual = false),
            spell(name = "Beta", level = 1, castingTime = "Action", concentration = false, ritual = false),
        )
        val levels = listOf(
            SpellLevelUiModel(level = 0, spells = spells.filter { it.level == 0 }),
            SpellLevelUiModel(level = 1, spells = spells.filter { it.level == 1 }),
        )

        val result = filterAndSortSpellLevels(
            spellLevels = levels,
            castingTime = null,
            concentrationOnly = false,
            ritualOnly = false,
            sort = SpellSort.Level,
        )

        assertThat(result.map { it.level }).containsExactly(0, 1).inOrder()
        assertThat(result.first().spells.map { it.name }).containsExactly("Alpha")
        assertThat(result[1].spells.map { it.name }).containsExactly("Beta", "Zeta").inOrder()
    }

    private fun spell(
        name: String,
        level: Int,
        castingTime: String,
        concentration: Boolean,
        ritual: Boolean,
    ): CharacterSpellUiModel {
        return CharacterSpellUiModel(
            spellId = name.lowercase(),
            name = name,
            level = level,
            school = "Evocation",
            castingTime = castingTime,
            range = "60 ft",
            components = listOf("V", "S"),
            ritual = ritual,
            concentration = concentration,
            sourceClass = "Wizard",
            sourceLabel = "Wizard",
            sourceKey = "wizard",
        )
    }
}

