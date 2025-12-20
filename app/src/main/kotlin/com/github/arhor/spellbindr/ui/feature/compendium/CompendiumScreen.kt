@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen

@Composable
fun CompendiumRoute(
    vm: CompendiumViewModel,
    onSpellSelected: (Spell) -> Unit,
) {
    val state by vm.state.collectAsState()

    CompendiumScreen(
        state = state,
        onSpellSelected = onSpellSelected,
        onSpellQueryChanged = vm::onQueryChanged,
        onSpellFiltersClick = vm::onFilterClicked,
        onSpellFavoriteClick = vm::onFavoritesClicked,
        onSpellSubmitFilters = vm::onFilterChanged,
        onSpellCancelFilters = vm::onFilterChanged,
        onConditionClick = vm::handleConditionClick,
        onAlignmentClick = vm::handleAlignmentClick,
        onRaceClick = vm::handleRaceClick,
    )
}

@Composable
private fun CompendiumScreen(
    state: CompendiumViewModel.State,
    modifier: Modifier = Modifier,
    onSpellSelected: (Spell) -> Unit = {},
    onSpellQueryChanged: (String) -> Unit,
    onSpellFiltersClick: () -> Unit,
    onSpellFavoriteClick: () -> Unit,
    onSpellSubmitFilters: (Set<EntityRef>) -> Unit,
    onSpellCancelFilters: (Set<EntityRef>) -> Unit,
    onConditionClick: (Condition) -> Unit,
    onAlignmentClick: (String) -> Unit,
    onRaceClick: (String) -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf(CompendiumSection.Spells) }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedSection.ordinal) {
            CompendiumSection.entries.forEach { section ->
                Tab(
                    selected = section == selectedSection,
                    onClick = { selectedSection = section },
                    text = { Text(section.label) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = selectedSection,
                label = "compendium-sections",
            ) { section ->
                when (section) {
                    CompendiumSection.Spells -> {
                        SpellSearchScreen(
                            state = state.spellsState,
                            onQueryChanged = onSpellQueryChanged,
                            onFiltersClick = onSpellFiltersClick,
                            onFavoriteClick = onSpellFavoriteClick,
                            onSpellClick = onSpellSelected,
                            onSubmitFilters = onSpellSubmitFilters,
                            onCancelFilters = onSpellCancelFilters,
                        )
                    }

                    CompendiumSection.Conditions -> {
                        ConditionsRoute(
                            state = state.conditionsState,
                            onConditionClick = onConditionClick,
                        )
                    }

                    CompendiumSection.Alignments -> {
                        AlignmentsRoute(
                            state = state.alignmentsState,
                            onAlignmentClick = onAlignmentClick,
                        )
                    }

                    CompendiumSection.Races -> {
                        RacesRoute(
                            state = state.racesState,
                            onRaceClick = onRaceClick,
                        )
                    }
                }
            }
        }
    }
}

private enum class CompendiumSection(val label: String) {
    Spells("Spells"),
    Conditions("Conditions"),
    Alignments("Alignments"),
    Races("Races"),
}
