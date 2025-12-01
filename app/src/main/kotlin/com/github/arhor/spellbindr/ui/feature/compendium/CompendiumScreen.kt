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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsScreen
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsScreen
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchViewModel
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.predefined.Condition

@Composable
fun CompendiumScreen(
    spellSearchState: SpellSearchViewModel.State,
    modifier: Modifier = Modifier,
    onSpellSelected: (String) -> Unit = {},
    onSpellQueryChanged: (String) -> Unit,
    onSpellFiltersClick: () -> Unit,
    onSpellFavoriteClick: () -> Unit,
    onSpellSubmitFilters: (Set<EntityRef>) -> Unit,
    onSpellCancelFilters: (Set<EntityRef>) -> Unit,
    conditionsState: ConditionsViewModel.State,
    onConditionClick: (Condition) -> Unit,
    alignmentsState: AlignmentsViewModel.State,
    onAlignmentClick: (String) -> Unit,
    racesState: RacesViewModel.State,
    onRaceClick: (String) -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf(CompendiumSection.Spells) }

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text("Compendium") },
        )
    ) {
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
                                state = spellSearchState,
                                onQueryChanged = onSpellQueryChanged,
                                onFiltersClick = onSpellFiltersClick,
                                onFavoriteClick = onSpellFavoriteClick,
                                onSpellClick = onSpellSelected,
                                onSubmitFilters = onSpellSubmitFilters,
                                onCancelFilters = onSpellCancelFilters,
                            )
                        }

                        CompendiumSection.Conditions -> {
                            ConditionsScreen(
                                state = conditionsState,
                                onConditionClick = onConditionClick,
                            )
                        }

                        CompendiumSection.Alignments -> {
                            AlignmentsScreen(
                                state = alignmentsState,
                                onAlignmentClick = onAlignmentClick,
                            )
                        }

                        CompendiumSection.Races -> {
                            RacesScreen(
                                state = racesState,
                                onRaceClick = onRaceClick,
                            )
                        }
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
