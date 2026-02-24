package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.utils.standardArray

internal val StandardArray: List<Int> = standardArray()

internal fun defaultStandardArrayAssignments(): Map<AbilityId, Int?> =
    AbilityIds.standardOrder.associateWith { null }

internal fun defaultPointBuyScores(): Map<AbilityId, Int> =
    AbilityIds.standardOrder.associateWith { 8 }

internal fun pointBuyCost(score: Int): Int = when (score) {
    8 -> 0
    9 -> 1
    10 -> 2
    11 -> 3
    12 -> 4
    13 -> 5
    14 -> 7
    15 -> 9
    else -> Int.MAX_VALUE
}
