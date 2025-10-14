package com.github.arhor.spellbindr.ui.feature.diceRoller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.components.BaseScreen
import com.github.arhor.spellbindr.ui.feature.diceRoller.components.DiceTypeSelector
import com.github.arhor.spellbindr.ui.feature.diceRoller.components.QuantityModifierSelector
import com.github.arhor.spellbindr.ui.feature.diceRoller.components.RollButton
import com.github.arhor.spellbindr.ui.feature.diceRoller.components.RollHistory
import com.github.arhor.spellbindr.ui.feature.diceRoller.components.RollResult

@Composable
fun DiceRollerScreen(
    viewModel: DiceRollerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BaseScreen {
        // Header
        Text(
            text = "Dice Roller",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Dice Type Selection
        DiceTypeSelector(
            selectedDiceType = state.selectedDiceType,
            onDiceTypeSelected = viewModel::selectDiceType
        )

        // Quantity and Modifier Selection
        QuantityModifierSelector(
            quantity = state.selectedQuantity,
            onQuantityChange = viewModel::updateQuantity,
        )

        // Roll Button
        RollButton(
            isRolling = state.isRolling,
            onClick = viewModel::rollDice
        )

        // Current Roll Result
        if (state.currentRoll != null) {
            RollResult(rollSet = state.currentRoll)
        }

        // History Section
        if (state.rollHistory.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 Roll History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = viewModel::toggleHistory
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Toggle history"
                            )
                        }

                        IconButton(
                            onClick = viewModel::clearHistory
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear history"
                            )
                        }
                    }
                }

                if (state.showHistory) {
                    RollHistory(
                        rollHistory = state.rollHistory,
                        onDeleteRoll = viewModel::deleteRoll
                    )
                }
            }
        }
    }
}
