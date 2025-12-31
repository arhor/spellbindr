package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompendiumSpellsRoute(
    vm: SpellsViewModel,
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state = vm.spellsState.collectAsStateWithLifecycle().value

    LaunchedEffect(vm) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is SpellsViewModel.Effect.SpellSelected -> onSpellSelected(effect.spell)
            }
        }
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Spells") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        SpellSearchScreen(
            state = state,
            onQueryChanged = { vm.onAction(SpellsViewModel.Action.QueryChanged(it)) },
            onFiltersClick = { vm.onAction(SpellsViewModel.Action.FiltersClicked) },
            onFavoriteClick = { vm.onAction(SpellsViewModel.Action.FavoritesToggled) },
            onGroupToggle = { vm.onAction(SpellsViewModel.Action.GroupToggled(it)) },
            onToggleAllGroups = { vm.onAction(SpellsViewModel.Action.ToggleAllGroups) },
            onSpellClick = { vm.onAction(SpellsViewModel.Action.SpellClicked(it)) },
            onSubmitFilters = { vm.onAction(SpellsViewModel.Action.FiltersSubmitted(it)) },
            onCancelFilters = { vm.onAction(SpellsViewModel.Action.FiltersCanceled(it)) },
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
                    visible = true,
                    title = { Text(text = "Spells") },
                    navigation = AppTopBarNavigation.Back(onClick = {}),
                ),
            ),
        ) {
            SpellSearchScreen(
                state = SpellsViewModel.SpellsState(
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
