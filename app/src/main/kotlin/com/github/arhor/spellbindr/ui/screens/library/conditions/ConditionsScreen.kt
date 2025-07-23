package com.github.arhor.spellbindr.ui.screens.library.conditions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConditionsScreen(
    viewModel: ConditionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var expandedItemName by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.conditions,
            key = { it.name }
        ) { condition ->
            ConditionListItem(
                condition = condition,
                isExpanded = condition.name == expandedItemName,
                onItemClick = {
                    expandedItemName = if (condition.name == expandedItemName) {
                        null
                    } else {
                        condition.name
                    }
                },
            )
        }
    }
}
