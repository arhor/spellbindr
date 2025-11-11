package com.github.arhor.spellbindr.ui.feature.dice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.feature.dice.components.LatestResultBar
import com.github.arhor.spellbindr.ui.feature.dice.components.MainDiceRollerCard
import com.github.arhor.spellbindr.ui.feature.dice.components.PercentileCard
import com.github.arhor.spellbindr.ui.feature.dice.components.RollDetailsSheetContent
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerIntent
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerState
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import kotlinx.coroutines.launch

@Composable
fun DiceRollerScreen(
    viewModel: DiceRollerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    DiceRollerScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollerScreen(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showDetails by remember { mutableStateOf(false) }
    var latestVisible by remember { mutableStateOf(false) }
    var dismissedToken by remember { mutableStateOf<Long?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.latestResultToken, state.latestResult) {
        val result = state.latestResult
        val token = state.latestResultToken
        if (result == null) {
            showDetails = false
            latestVisible = false
            dismissedToken = null
        } else if (token != dismissedToken) {
            showDetails = false
            latestVisible = true
        } else {
            latestVisible = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Dice Roller") },
                actions = {
                    IconButton(onClick = { /* Stub: future history action */ }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "History",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 180.dp),
            ) {
                MainDiceRollerCard(
                    state = state,
                    onIntent = onIntent,
                )
                PercentileCard(
                    lastPercentileRoll = state.lastPercentileRoll,
                    onRollPercentile = { onIntent(DiceRollerIntent.RollPercentile) },
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(modifier = Modifier.height(96.dp))
            }
            val latestResult = state.latestResult
            if (latestVisible && latestResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .dismissOnTap {
                            latestVisible = false
                            showDetails = false
                            dismissedToken = state.latestResultToken
                        },
                )
                LatestResultBar(
                    latestResult = latestResult,
                    onReRoll = { onIntent(DiceRollerIntent.ReRollLast) },
                    onShowDetails = { showDetails = true },
                    onClose = {
                        showDetails = false
                        latestVisible = false
                        dismissedToken = state.latestResultToken
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .consumeClicks(),
                )
            }
        }
    }

    val latestResult = state.latestResult
    if (showDetails && latestResult != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetails = false },
            sheetState = sheetState,
        ) {
            RollDetailsSheetContent(
                result = latestResult,
                onClose = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showDetails = false
                        }
                    }
                },
            )
        }
    }
}

private fun Modifier.dismissOnTap(
    onDismiss: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onDismiss,
    )
}

private fun Modifier.consumeClicks(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = {},
    )
}

@Preview
@Composable
private fun DiceRollerScreenPreview() {
    DiceRollerScreen(
        state = DiceRollerState(
            hasCheck = true,
            checkMode = CheckMode.ADVANTAGE,
            checkModifier = 5,
            amountDice = listOf(DiceGroup(sides = 8, count = 2)),
            lastPercentileRoll = 57,
            latestResult = RollResult.CheckAmountResult(
                check = CheckResult(
                    mode = CheckMode.ADVANTAGE,
                    rolls = listOf(9, 18),
                    modifier = 5,
                    keptRoll = 18,
                    total = 23,
                ),
                amount = AmountResult(
                    groups = listOf(
                        DiceGroupResult(
                            sides = 8,
                            rolls = listOf(5, 6),
                        ),
                    ),
                    total = 14,
                ),
            ),
        ),
        onIntent = {},
    )
}
