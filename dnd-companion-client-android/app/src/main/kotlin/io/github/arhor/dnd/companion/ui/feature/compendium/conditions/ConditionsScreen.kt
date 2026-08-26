package io.github.arhor.dnd.companion.ui.feature.compendium.conditions

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
import io.github.arhor.dnd.companion.domain.model.Condition
import io.github.arhor.dnd.companion.ui.components.ErrorMessage
import io.github.arhor.dnd.companion.ui.components.LoadingIndicator
import io.github.arhor.dnd.companion.ui.feature.compendium.conditions.components.ConditionListItem
import io.github.arhor.dnd.companion.ui.theme.AppTheme
import io.github.arhor.dnd.companion.utils.scrollToItemIfNeeded

@Composable
internal fun ConditionsScreen(
    uiState: ConditionsUiState,
    dispatch: ConditionsDispatch = {},
) {
    when (uiState) {
        is ConditionsUiState.Loading -> LoadingIndicator()
        is ConditionsUiState.Failure -> ErrorMessage(uiState.errorMessage)
        is ConditionsUiState.Content -> ConditionsContent(uiState, dispatch)
    }
}

@Composable
fun ConditionsContent(
    uiState: ConditionsUiState.Content,
    dispatch: ConditionsDispatch,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.selectedItemId, uiState.conditions) {
        listState.scrollToItemIfNeeded(
            items = uiState.conditions,
            selector = Condition::id,
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
        items(items = uiState.conditions, key = { it.id }) { condition ->
            ConditionListItem(
                condition = condition,
                isExpanded = condition.id == uiState.selectedItemId,
                onItemClick = { dispatch(ConditionsIntent.ConditionClicked(condition.id)) },
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun ConditionsScreenPreview() {
    AppTheme {
        ConditionsScreen(
            uiState = ConditionsUiState.Content(
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
