@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.pointBuyCost
import com.github.arhor.spellbindr.utils.calculatePointBuyCost

@Composable
internal fun PointBuyAssign(
    state: GuidedCharacterSetupUiState.Content,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
) {
    val totalCost = calculatePointBuyCost(state.selection.pointBuyScores)
    val remaining = (27 - totalCost).coerceAtLeast(0)

    Text(
        text = "Points remaining: $remaining (spent $totalCost / 27)",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AbilityIds.standardOrder.forEach { abilityId ->
        val value = state.selection.pointBuyScores[abilityId] ?: 8
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = abilityId.displayName(), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Cost: ${pointBuyCost(value)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = { onPointBuyDecrement(abilityId) },
                    enabled = value > 8,
                ) {
                    Text("-")
                }
                Text(text = value.toString(), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { onPointBuyIncrement(abilityId) },
                    enabled = value < 15,
                ) {
                    Text("+")
                }
            }
        }
    }
}
