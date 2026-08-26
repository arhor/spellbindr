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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.guided.components.SelectRow
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod

@Composable
internal fun AbilityMethodStep(
    state: GuidedCharacterSetupUiState.Content,
    onAbilityMethodSelected: (AbilityScoreMethod) -> Unit,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Ability score method", style = MaterialTheme.typography.titleMedium) }
        items(AbilityScoreMethod.entries, key = { it.name }) { method ->
            SelectRow(
                title = method.label,
                selected = state.selection.abilityMethod == method,
                onClick = { onAbilityMethodSelected(method) },
            )
        }
        item {
            Text(
                text = "Racial ability score increases are applied after this step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

