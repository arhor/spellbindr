package com.github.arhor.spellbindr.ui.feature.compendium.conditions

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
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel

@Composable
fun ConditionsRoute(
    state: CompendiumViewModel.ConditionsState,
    onConditionClick: (Condition) -> Unit,
) {
    ConditionsScreen(
        state = state,
        onConditionClick = onConditionClick,
    )
}

@Composable
private fun ConditionsScreen(
    state: CompendiumViewModel.ConditionsState,
    onConditionClick: (Condition) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        val index = Condition.entries.indexOfFirst { it == state.expandedItem }
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
        items(items = Condition.entries, key = { it.displayName }) { condition ->
            ConditionListItem(
                condition = condition,
                isExpanded = condition == state.expandedItem,
                onItemClick = { onConditionClick(condition) },
            )
        }
    }
}
