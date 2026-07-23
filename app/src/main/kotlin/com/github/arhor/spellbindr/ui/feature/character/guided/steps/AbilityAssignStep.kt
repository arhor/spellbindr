@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod

@Composable
internal fun AbilityAssignStep(
    state: GuidedCharacterSetupUiState.Content,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
    listState: LazyListState,
) {
    val method = state.selection.abilityMethod
    if (method == null) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Choose a method first.") }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Assign scores", style = MaterialTheme.typography.titleMedium) }
        item {
            when (method) {
                AbilityScoreMethod.STANDARD_ARRAY -> StandardArrayAssign(state, onStandardArrayAssigned)
                AbilityScoreMethod.POINT_BUY -> PointBuyAssign(state, onPointBuyIncrement, onPointBuyDecrement)
            }
        }
    }
}

