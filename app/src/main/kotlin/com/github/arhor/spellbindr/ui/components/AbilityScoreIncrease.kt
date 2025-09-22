package com.github.arhor.spellbindr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.AbilityScores
import com.github.arhor.spellbindr.utils.PreviewScope
import com.github.arhor.spellbindr.utils.calculatePointBuyCost
import com.github.arhor.spellbindr.utils.signed

private const val TOTAL_POINTS = 27
private const val ABILITY_SCORE_MIN = 8
private const val ABILITY_SCORE_MAX = 15
private const val ABILITY_BONUS_MIN = 0

@Composable
fun AbilityScoreIncrease(
    racialBonuses: Map<Ability, Int>,
) {
    var currentMethod by remember { mutableStateOf(Method.POINT_BUY) }
    var abilityScores by remember(currentMethod) {
        mutableStateOf(
            when (currentMethod) {
                Method.POINT_BUY -> AbilityScores()
                Method.STD_ARRAY -> AbilityScores.STANDARD_ARRAY
            }
        )
    }

    fun handleMethodChange(method: Method): () -> Unit = {
        if (method != currentMethod) {
            currentMethod = method
        }
    }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Character Ability Scores",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(modifier = Modifier.selectableGroup()) {
            Method.entries.forEach { method ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .selectable(
                            selected = (method == currentMethod),
                            onClick = handleMethodChange(method),
                            role = Role.RadioButton,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = (method == currentMethod),
                        onClick = null,
                    )
                    Text(
                        text = method.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }
        for (ability in Ability.entries) {
            AbilitiesListItem(
                method = currentMethod,
                ability = ability,
                abilityScores = abilityScores,
                racialBonuses = racialBonuses,
                onAbilitiesChanged = { abilityScores = abilityScores.copy(it) },
            )
        }
        GradientDivider(modifier = Modifier.padding(vertical = 16.dp))

        when (currentMethod) {
            Method.POINT_BUY -> {
                RemainingPointsSection(abilityScores)
            }

            Method.STD_ARRAY -> {
                Text(
                    text = "Distribute standard points array across your abilities: ${
                        AbilityScores.STANDARD_ARRAY.values.joinToString()
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

interface AbilityListItemController {
    val canDecrease: Boolean
    val canIncrease: Boolean
    fun handleDecreaseClick()
    fun handleIncreaseClick()
}

@Composable
private fun AbilitiesListItem(
    method: Method,
    ability: Ability,
    abilityScores: AbilityScores,
    racialBonuses: Map<Ability, Int>,
    onAbilitiesChanged: (Map<Ability, Int>) -> Unit,
) {
    val abilityScore = abilityScores[ability] ?: ABILITY_SCORE_MIN
    val abilityBonus = racialBonuses[ability] ?: ABILITY_BONUS_MIN
    val controller = createAbilityListItemController(method, ability, abilityScore, abilityScores, onAbilitiesChanged)
    val abilityScoreDisplayValue = if (abilityBonus != 0) {
        "$abilityScore (${signed(abilityBonus)})"
    } else {
        "$abilityScore"
    }

    ListItem(
        headlineContent = {
            Text(
                text = ability.displayName,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.35f)
            ) {
                AbilityScoreChangeButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    enabled = controller.canDecrease,
                    onClick = controller::handleDecreaseClick,
                )
                Text(
                    text = abilityScoreDisplayValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                AbilityScoreChangeButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    enabled = controller.canIncrease,
                    onClick = controller::handleIncreaseClick,
                )
            }
        },
    )
}

private fun createAbilityListItemController(
    method: Method,
    currAbility: Ability,
    currAbilityScore: Int,
    abilityScores: AbilityScores,
    onAbilitiesChanged: (Map<Ability, Int>) -> Unit
): AbilityListItemController = when (method) {
    Method.POINT_BUY -> object : AbilityListItemController {
        override val canDecrease = currAbilityScore > ABILITY_SCORE_MIN
        override val canIncrease = currAbilityScore < ABILITY_SCORE_MAX

        override fun handleDecreaseClick() {
            if (canDecrease) {
                onAbilitiesChanged(mapOf(currAbility to currAbilityScore - 1))
            }
        }

        override fun handleIncreaseClick() {
            if (canIncrease) {
                val nextAbilityScoreValue = currAbilityScore + 1
                val nexAbilityScores = abilityScores + (currAbility to nextAbilityScoreValue)
                val nextPointBuyCost = calculatePointBuyCost(nexAbilityScores)

                if (nextPointBuyCost <= TOTAL_POINTS) {
                    onAbilitiesChanged(mapOf(currAbility to nextAbilityScoreValue))
                }
            }
        }
    }

    Method.STD_ARRAY -> object : AbilityListItemController {
        override val canDecrease = true
        override val canIncrease = true

        override fun handleDecreaseClick() {
            swapAbilityScoresByIndex { currIndex, lastIndex -> if (currIndex < lastIndex) currIndex + 1 else 0 }
        }

        override fun handleIncreaseClick() {
            swapAbilityScoresByIndex { currIndex, lastIndex -> if (currIndex > 0) currIndex - 1 else lastIndex }
        }

        private fun swapAbilityScoresByIndex(calculateNextIndex: (Int, Int) -> Int) {
            val abilities = abilityScores.entries.toList()
            val currIndex = abilities.indexOfFirst { it.key == currAbility }
            val nextIndex = calculateNextIndex(currIndex, abilities.lastIndex)
            val (nextAbility, nextAbilityScore) = abilities[nextIndex]

            onAbilitiesChanged(
                mapOf(
                    currAbility to nextAbilityScore,
                    nextAbility to currAbilityScore,
                )
            )
        }
    }
}

@Composable
private fun RemainingPointsSection(abilityScores: AbilityScores) {
    val currPointsSpent = calculatePointBuyCost(abilityScores)
    val remainingPoints = TOTAL_POINTS - currPointsSpent
    val spentPointsRate by animateFloatAsState(
        targetValue = remainingPoints / TOTAL_POINTS.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Text(
        text = "Points Remaining: $remainingPoints",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(
        modifier = Modifier.height(16.dp),
    )
    LinearProgressIndicator(
        progress = { spentPointsRate },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
    )
}

@Composable
private fun AbilityScoreChangeButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(25.dp),
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}

enum class Method(val displayName: String) {
    POINT_BUY("Point buy"),
    STD_ARRAY("Simple"),
}

@Preview
@Composable
private fun AbilityScoreIncreasePreview() {
    PreviewScope {
        AbilityScoreIncrease(
            racialBonuses = mapOf(
                Ability.STR to +2,
                Ability.DEX to +1,
                Ability.INT to -3,
            ),
        )
    }
}
