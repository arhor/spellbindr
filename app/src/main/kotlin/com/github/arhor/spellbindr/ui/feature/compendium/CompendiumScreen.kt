@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumAction
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumUiState
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsRoute
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen

@Composable
fun CompendiumRoute(
    vm: CompendiumViewModel,
    onSpellSelected: (Spell) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Compendium") },
            ),
        ),
    ) {
        CompendiumScreen(
            state = state,
            onAction = vm::onAction,
            onSpellSelected = onSpellSelected,
        )
    }
}

@Composable
private fun CompendiumScreen(
    state: CompendiumUiState,
    modifier: Modifier = Modifier,
    onAction: (CompendiumAction) -> Unit,
    onSpellSelected: (Spell) -> Unit = {},
) {
    when (state) {
        CompendiumUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is CompendiumUiState.Content -> {
            CompendiumContent(
                state = state,
                modifier = modifier,
                onAction = onAction,
                onSpellSelected = onSpellSelected,
            )
        }

        is CompendiumUiState.Error -> {
            state.content?.let { content ->
                CompendiumContent(
                    state = content,
                    modifier = modifier,
                    onAction = onAction,
                    onSpellSelected = onSpellSelected,
                )
            }
        }
    }
}

@Composable
private fun CompendiumContent(
    state: CompendiumUiState.Content,
    modifier: Modifier = Modifier,
    onAction: (CompendiumAction) -> Unit,
    onSpellSelected: (Spell) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = state.selectedSection.ordinal) {
            CompendiumSection.entries.forEach { section ->
                Tab(
                    selected = section == state.selectedSection,
                    onClick = { onAction(CompendiumAction.SectionSelected(section)) },
                    text = { Text(section.label) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = state.selectedSection,
                label = "compendium-sections",
            ) { section ->
                when (section) {
                    CompendiumSection.Spells -> {
                        SpellSearchScreen(
                            state = state.spellsState,
                            onQueryChanged = { query ->
                                onAction(CompendiumAction.SpellQueryChanged(query))
                            },
                            onFiltersClick = { onAction(CompendiumAction.SpellFiltersClicked) },
                            onFavoriteClick = { onAction(CompendiumAction.SpellFavoritesToggled) },
                            onGroupToggle = { level ->
                                onAction(CompendiumAction.SpellGroupToggled(level))
                            },
                            onToggleAllGroups = { onAction(CompendiumAction.SpellToggleAllGroups) },
                            onSpellClick = onSpellSelected,
                            onSubmitFilters = { classes: Set<EntityRef> ->
                                onAction(CompendiumAction.SpellFiltersSubmitted(classes))
                            },
                            onCancelFilters = { classes: Set<EntityRef> ->
                                onAction(CompendiumAction.SpellFiltersCanceled(classes))
                            },
                        )
                    }

                    CompendiumSection.Conditions -> {
                        ConditionsRoute(
                            state = state.conditionsState,
                            onConditionClick = { condition: Condition ->
                                onAction(CompendiumAction.ConditionClicked(condition))
                            },
                        )
                    }

                    CompendiumSection.Alignments -> {
                        AlignmentsRoute(
                            state = state.alignmentsState,
                            onAlignmentClick = { name ->
                                onAction(CompendiumAction.AlignmentClicked(name))
                            },
                        )
                    }

                    CompendiumSection.Races -> {
                        RacesRoute(
                            state = state.racesState,
                            onRaceClick = { raceName ->
                                onAction(CompendiumAction.RaceClicked(raceName))
                            },
                        )
                    }
                }
            }
        }
    }
}
