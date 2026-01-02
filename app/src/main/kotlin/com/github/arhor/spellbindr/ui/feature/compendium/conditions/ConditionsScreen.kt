package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun ConditionsScreen(
    state: ConditionsUiState,
    onConditionClick: (Condition) -> Unit,
) {
    when (state) {
        is ConditionsUiState.Loading -> {
            ConditionLoader()
        }

        is ConditionsUiState.Content -> {
            ConditionsContent(state, onConditionClick)
        }

        is ConditionsUiState.Error -> {
            ConditionError(state)
        }
    }
}

@Composable
private fun ConditionLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
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
private fun ConditionError(state: ConditionsUiState.Error) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = state.message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun ConditionListItem(
    condition: Condition,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onItemClick,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
        ) {
            Text(
                text = condition.displayName,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    GradientDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = condition.description.joinToString(separator = "\n\n"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
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
            onConditionClick = {},
        )
    }
}
