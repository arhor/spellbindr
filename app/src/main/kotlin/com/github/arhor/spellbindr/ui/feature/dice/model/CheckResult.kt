package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class CheckResult(
    val mode: CheckMode,
    val rolls: List<Int>,
    val modifier: Int,
    val keptRoll: Int,
    val total: Int,
)
