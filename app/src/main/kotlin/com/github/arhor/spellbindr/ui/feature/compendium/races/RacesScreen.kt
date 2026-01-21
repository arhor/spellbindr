package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.races.components.RaceListItem
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.scrollToItemIfNeeded

@Composable
internal fun RacesScreen(
    uiState: RacesUiState,
    onRaceClick: (Race) -> Unit = {},
) {
    when (uiState) {
        is RacesUiState.Loading -> LoadingIndicator()
        is RacesUiState.Failure -> ErrorMessage(uiState.errorMessage)
        is RacesUiState.Content -> RacesContent(uiState, onRaceClick)
    }
}

@Composable
private fun RacesContent(
    uiState: RacesUiState.Content,
    onRaceClick: (Race) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState) {
        listState.scrollToItemIfNeeded(
            items = uiState.races,
            selector = Race::id,
            selectedKey = uiState.selectedItemId,
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = uiState.races, key = { it.id }) { race ->
            RaceListItem(
                race = race,
                traits = uiState.traits,
                isExpanded = race.id == uiState.selectedItemId,
                onItemClick = { onRaceClick(race) }
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun RacesScreenPreview() {
    val darkvision = Trait(
        id = "darkvision",
        name = "Darkvision",
        desc = listOf("Accustomed to twilit forests and the night sky, you can see in dim light within 60 feet."),
    )
    val keenSenses = Trait(
        id = "keen_senses",
        name = "Keen Senses",
        desc = listOf("You have proficiency in the Perception skill."),
    )
    val race = Race(
        id = "elf",
        name = "Elf",
        traits = listOf(EntityRef(id = darkvision.id)),
        subraces = listOf(
            Race.Subrace(
                id = "high_elf",
                name = "High Elf",
                desc = "Elves with keen intellect and magic affinity.",
                traits = listOf(EntityRef(id = keenSenses.id)),
            )
        ),
    )

    AppTheme {
        RacesScreen(
            uiState = RacesUiState.Content(
                races = listOf(race),
                traits = mapOf(
                    darkvision.id to darkvision,
                    keenSenses.id to keenSenses,
                ),
                selectedItemId = race.id,
            ),
        )
    }
}
