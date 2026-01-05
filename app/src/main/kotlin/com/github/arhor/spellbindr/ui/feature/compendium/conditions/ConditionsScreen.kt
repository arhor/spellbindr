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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.components.ConditionListItem
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun ConditionsScreen(
    state: ConditionsUiState,
    onConditionClick: (Condition) -> Unit = {},
) {
    when (state) {
        is ConditionsUiState.Loading -> LoadingIndicator()
        is ConditionsUiState.Content -> ConditionsContent(state, onConditionClick)
        is ConditionsUiState.Error -> ErrorMessage(state.errorMessage)
    }
}

@Composable
fun ConditionsContent(
    state: ConditionsUiState.Content,
    onConditionClick: (Condition) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        val index = state.conditions.indexOfFirst { it.id == state.selectedItemId }
        if (index != -1) {
            val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
            if (itemInfo == null || itemInfo.offset + itemInfo.size > listState.layoutInfo.viewportSize.height) {
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
        items(items = state.conditions, key = { it.id }) { condition ->
            ConditionListItem(
                condition = condition,
                isExpanded = condition.id == state.selectedItemId,
                onItemClick = { onConditionClick(condition) },
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun ConditionsScreenPreview() {
    AppTheme {
        ConditionsScreen(
            state = ConditionsUiState.Content(
                conditions = listOf(
                    Condition(
                        id = "blinded",
                        displayName = "Blinded",
                        description = listOf(
                            "- A blinded creature can't see and automatically fails any ability check that requires sight.",
                            "- Attack rolls against the creature have advantage, and the creature's attack rolls have disadvantage.",
                        ),
                    ),
                )
            ),
        )
    }
}
