package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.ui.components.D20HpBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun CombatOverviewCard(
    modifier: Modifier = Modifier,
    header: CharacterHeaderUiState,
    abilities: List<AbilityUiModel>,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        val abilitiesByType = abilities.associateBy(AbilityUiModel::ability)
        val leftAbilities = LEFT_ABILITY_ORDER.mapNotNull { abilitiesByType[it]?.toAbilityScore() }
        val rightAbilities = RIGHT_ABILITY_ORDER.mapNotNull { abilitiesByType[it]?.toAbilityScore() }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AbilityScoreColumn(
                    abilities = leftAbilities,
                )
                D20HpBar(
                    currentHp = header.hitPoints.current,
                    maxHp = header.hitPoints.max,
                    modifier = Modifier.weight(1f),
                )
                AbilityScoreColumn(
                    abilities = rightAbilities,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            StatsCard(
                ac = header.armorClass,
                initiative = header.initiative,
                speed = header.speed,
            )
        }
    }
}

@Composable
private fun StatsCard(
    ac: Int,
    initiative: Int,
    speed: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatBlock(title = "AC", value = ac.toString())
            StatDivider()
            StatBlock(title = "Initiative", value = formatBonus(initiative))
            StatDivider()
            StatBlock(title = "Speed", value = speed)
        }
    }
}

@Composable
private fun RowScope.StatBlock(
    title: String,
    value: String,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatDivider() {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
}

@Composable
private fun AbilityScoreColumn(
    abilities: List<AbilityScore>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        abilities.forEach { ability ->
            AbilityScoreCardV2(ability = ability)
        }
    }
}

private fun AbilityUiModel.toAbilityScore(): AbilityScore {
    return AbilityScore(
        name = ability.name,
        value = score,
        bonus = modifier,
    )
}

private val LEFT_ABILITY_ORDER = listOf(Ability.STR, Ability.DEX, Ability.CON)
private val RIGHT_ABILITY_ORDER = listOf(Ability.INT, Ability.WIS, Ability.CHA)

@Preview
@Composable
private fun CombatOverviewCardPreview() {
    AppTheme {
        CombatOverviewCard(
            header = CharacterSheetPreviewData.header,
            abilities = CharacterSheetPreviewData.overview.abilities,
        )
    }
}
