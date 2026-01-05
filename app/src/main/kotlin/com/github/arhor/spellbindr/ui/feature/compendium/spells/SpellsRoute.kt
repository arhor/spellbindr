package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsRoute(
    vm: SpellsViewModel = hiltViewModel(),
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Spells",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        SpellsScreen(
            state = state,
            onQueryChanged = vm::onQueryChanged,
            onFiltersClick = vm::onFiltersClick,
            onFavoriteClick = vm::onFavoritesToggled,
            onGroupToggle = vm::onGroupToggled,
            onToggleAllGroups = vm::onToggleAllGroups,
            onSpellClick = onSpellSelected,
            onSubmitFilters = vm::onFiltersSubmitted,
            onCancelFilters = vm::onFiltersCanceled,
        )
    }
}

@Preview
@Composable
private fun CompendiumSpellsRoutePreview() {
    val previewSpell = Spell(
        id = "healing_word",
        name = "Healing Word",
        desc = listOf("A creature of your choice that you can see regains hit points."),
        level = 1,
        range = "60 ft",
        ritual = false,
        school = EntityRef(id = "evocation"),
        duration = "Instant",
        castingTime = "1 bonus action",
        classes = listOf(EntityRef(id = "cleric")),
        components = listOf("V"),
        concentration = false,
        source = "PHB",
    )
    val spells = listOf(
        previewSpell.copy(level = 0, name = "Sacred Flame"),
        previewSpell.copy(id = "cure_wounds", name = "Cure Wounds"),
    )
    val spellsByLevel = spells.groupBy(Spell::level).toSortedMap()

    AppTheme {
        ProvideTopBarState(
            topBarState = TopBarState(
                config = AppTopBarConfig(
                    title = "Spells",
                    navigation = AppTopBarNavigation.Back(onClick = {}),
                ),
            ),
        ) {
            SpellsScreen(
                state = SpellsViewModel.State(
                    query = "heal",
                    spellsByLevel = spellsByLevel,
                    expandedSpellLevels = mapOf(0 to true, 1 to true),
                    expandedAll = true,
                    uiState = SpellsUiState.Loaded(
                        spells = spells,
                        spellsByLevel = spellsByLevel,
                    ),
                ),
                onQueryChanged = {},
                onFiltersClick = {},
                onFavoriteClick = {},
                onGroupToggle = {},
                onToggleAllGroups = {},
                onSpellClick = {},
                onSubmitFilters = {},
                onCancelFilters = {},
            )
        }
    }
}
