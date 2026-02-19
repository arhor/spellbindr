@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.components.SelectRowCompact
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.StandardArray

@Composable
internal fun StandardArrayAssign(
    state: GuidedCharacterSetupUiState.Content,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
) {
    Text(
        text = "Use values: ${StandardArray.joinToString()}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AbilityIds.standardOrder.forEach { abilityId ->
        val assigned = state.selection.standardArrayAssignments[abilityId]
        val takenByOthers = state.selection.standardArrayAssignments
            .filterKeys { it != abilityId }
            .values
            .filterNotNull()
            .toSet()

        val available = StandardArray.filter { it !in takenByOthers || it == assigned }

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = abilityId.displayName(), style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    available.forEach { value ->
                        SelectRowCompact(
                            label = value.toString(),
                            selected = assigned == value,
                            onClick = { onStandardArrayAssigned(abilityId, value) },
                        )
                    }
                    if (assigned != null) {
                        OutlinedButton(onClick = { onStandardArrayAssigned(abilityId, null) }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}

