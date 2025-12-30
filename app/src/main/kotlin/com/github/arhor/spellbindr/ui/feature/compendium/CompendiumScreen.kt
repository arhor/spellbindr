@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumAction
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen

@Composable
fun CompendiumSectionsRoute(
    onNavigateToSpells: () -> Unit,
    onNavigateToConditions: () -> Unit,
    onNavigateToAlignments: () -> Unit,
    onNavigateToRaces: () -> Unit,
    onNavigateToTraits: () -> Unit,
    onNavigateToFeatures: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToEquipment: () -> Unit,
) {
    val sections = listOf(
        CompendiumSectionEntry(title = "Spells", onClick = onNavigateToSpells),
        CompendiumSectionEntry(title = "Conditions", onClick = onNavigateToConditions),
        CompendiumSectionEntry(title = "Alignments", onClick = onNavigateToAlignments),
        CompendiumSectionEntry(title = "Races", onClick = onNavigateToRaces),
        CompendiumSectionEntry(title = "Traits", onClick = onNavigateToTraits),
        CompendiumSectionEntry(title = "Features", onClick = onNavigateToFeatures),
        CompendiumSectionEntry(title = "Classes", onClick = onNavigateToClasses),
        CompendiumSectionEntry(title = "Equipment", onClick = onNavigateToEquipment),
    )

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Compendium") },
            ),
        ),
    ) {
        CompendiumSectionsScreen(sections = sections)
    }
}

@Composable
fun CompendiumSpellsRoute(
    vm: CompendiumViewModel,
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.spellsState.collectAsStateWithLifecycle()

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
            onQueryChanged = { vm.onAction(CompendiumAction.SpellQueryChanged(it)) },
            onFiltersClick = { vm.onAction(CompendiumAction.SpellFiltersClicked) },
            onFavoriteClick = { vm.onAction(CompendiumAction.SpellFavoritesToggled) },
            onGroupToggle = { vm.onAction(CompendiumAction.SpellGroupToggled(it)) },
            onToggleAllGroups = { vm.onAction(CompendiumAction.SpellToggleAllGroups) },
            onSpellClick = onSpellSelected,
            onSubmitFilters = { vm.onAction(CompendiumAction.SpellFiltersSubmitted(it)) },
            onCancelFilters = { vm.onAction(CompendiumAction.SpellFiltersCanceled(it)) },
        )
    }
}

@Composable
fun CompendiumConditionsRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state by vm.conditionsState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Conditions") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        ConditionsRoute(
            state = state,
            onConditionClick = { condition: Condition ->
                vm.onAction(CompendiumAction.ConditionClicked(condition))
            },
        )
    }
}

@Composable
fun CompendiumAlignmentsRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state by vm.alignmentsState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Alignments") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        AlignmentsRoute(
            state = state,
            onAlignmentClick = { name ->
                vm.onAction(CompendiumAction.AlignmentClicked(name))
            },
        )
    }
}

@Composable
fun CompendiumRacesRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state by vm.racesState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Races") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        RacesRoute(
            state = state,
            onRaceClick = { raceName ->
                vm.onAction(CompendiumAction.RaceClicked(raceName))
            },
        )
    }
}

@Composable
fun CompendiumTraitsRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Traits", onBack = onBack)
}

@Composable
fun CompendiumFeaturesRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Features", onBack = onBack)
}

@Composable
fun CompendiumClassesRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Classes", onBack = onBack)
}

@Composable
fun CompendiumEquipmentRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Equipment", onBack = onBack)
}

@Composable
private fun CompendiumPlaceholderRoute(
    title: String,
    onBack: () -> Unit,
) {
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = title) },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "$title coming soon")
        }
    }
}

@Composable
private fun CompendiumSectionsScreen(sections: List<CompendiumSectionEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(sections) { section ->
            Card(
                modifier = Modifier.clickable(onClick = section.onClick),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = section.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                    section.description?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private data class CompendiumSectionEntry(
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit,
)
