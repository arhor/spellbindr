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
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesViewModel.State

@Composable
fun RacesRoute(
    state: State,
    onRaceClick: (String) -> Unit,
) {
    RacesScreen(
        state = state,
        onRaceClick = onRaceClick,
    )
}

@Composable
private fun RacesScreen(
    state: State,
    onRaceClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        val index = state.races.indexOfFirst { it.name == state.expandedItemName }
        if (index != -1) {
            val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
            if (itemInfo != null) {
                val viewportHeight = listState.layoutInfo.viewportSize.height
                if (itemInfo.offset + itemInfo.size > viewportHeight) {
                    listState.animateScrollToItem(index)
                }
            } else {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = state.races, key = { it.name }) {
            RaceListItem(
                race = it,
                traits = state.traits,
                isExpanded = it.name == state.expandedItemName,
                onItemClick = { onRaceClick(it.name) }
            )
        }
    }
}
