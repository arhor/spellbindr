@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.dice.components.LatestResultBar
import com.github.arhor.spellbindr.ui.feature.dice.components.MainDiceRollerCard
import com.github.arhor.spellbindr.ui.feature.dice.components.PercentileCard
import com.github.arhor.spellbindr.ui.feature.dice.components.RollDetailsSheetContent
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun DiceRollerScreen(
    state: DiceRollerUiState,
    dispatch: DiceRollerDispatch = {},
    modifier: Modifier = Modifier,
) {
    when (state) {
        is DiceRollerUiState.Loading -> {
            LoadingIndicator()
        }

        is DiceRollerUiState.Content -> {
            DiceRollerContent(
                state = state,
                dispatch = dispatch,
                modifier = modifier,
            )
        }

        is DiceRollerUiState.Error -> {
            ErrorMessage(state.errorMessage)
        }
    }
}

@Composable
private fun DiceRollerContent(
    state: DiceRollerUiState.Content,
    dispatch: DiceRollerDispatch,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showDetails by remember { mutableStateOf(false) }
    var latestVisible by remember { mutableStateOf(false) }
    var dismissedToken by remember { mutableStateOf<Long?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val latestResult = state.latestResult

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

    fun closeLatestResultDialog() {
        showDetails = false
        latestVisible = false
        dismissedToken = state.latestResultToken
    }

    Box(
        modifier = modifier.fillMaxSize(),
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
                onToggleCheck = { dispatch(DiceRollerIntent.ToggleCheck) },
                onCheckModeSelected = { dispatch(DiceRollerIntent.CheckModeSelected(it)) },
                onIncrementCheckModifier = { dispatch(DiceRollerIntent.IncrementCheckModifier) },
                onDecrementCheckModifier = { dispatch(DiceRollerIntent.DecrementCheckModifier) },
                onAddAmountDie = { dispatch(DiceRollerIntent.AddAmountDie(it)) },
                onIncrementAmountDie = { dispatch(DiceRollerIntent.IncrementAmountDie(it)) },
                onDecrementAmountDie = { dispatch(DiceRollerIntent.DecrementAmountDie(it)) },
                onClearAll = { dispatch(DiceRollerIntent.ClearAll) },
                onRollMain = { dispatch(DiceRollerIntent.RollMain) },
            )
            PercentileCard(
                lastPercentileRoll = state.lastPercentileRoll,
                onRollPercentile = { dispatch(DiceRollerIntent.RollPercentile) },
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(96.dp))
        }

        if (latestVisible && latestResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeClicks(::closeLatestResultDialog),
            )
            LatestResultBar(
                latestResult = latestResult,
                onReRoll = { dispatch(DiceRollerIntent.RerollLast) },
                onShowDetails = { showDetails = true },
                onClose = ::closeLatestResultDialog,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .consumeClicks(),
            )
        }
    }

    if (showDetails && latestResult != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetails = false },
            sheetState = sheetState,
        ) {
            RollDetailsSheetContent(
                result = latestResult,
                onClose = {
                    coroutineScope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion { if (!sheetState.isVisible) showDetails = false }
                },
            )
        }
    }
}

private fun Modifier.consumeClicks(
    onDismiss: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }

    clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onDismiss,
    )
}

@Composable
@PreviewLightDark
private fun DiceRollerScreenPreview() {
    AppTheme {
        DiceRollerScreen(
            state = DiceRollerUiState.Content(
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
        )
    }
}
