package com.github.arhor.spellbindr.ui.screens.library.conditions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConditionsScreen(
    viewModel: ConditionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        val index = state.conditions.indexOfFirst { it.name == state.expandedItemName }
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
        items(items = state.conditions, key = { it.name }) {
            ConditionListItem(
                condition = it,
                isExpanded = it.name == state.expandedItemName,
                onItemClick = { viewModel.handleConditionClick(it.name) }
            )
        }
    }
}
