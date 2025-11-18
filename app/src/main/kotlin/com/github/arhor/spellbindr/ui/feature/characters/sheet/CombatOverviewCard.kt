package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.components.D20HpBar
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun CombatOverviewCard(
    modifier: Modifier = Modifier,
    header: CharacterHeaderUiState,
    onDamageClick: () -> Unit,
    onHealClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        ContentView(
            header = header,
            onDamageClick = onDamageClick,
            onHealClick = onHealClick
        )
    }
}

@Preview
@Composable
private fun CombatOverviewCardPreview() {
    AppTheme {
        CombatOverviewCard(
            header = CharacterSheetPreviewData.header,
            onDamageClick = {},
            onHealClick = {},
        )
    }
}

@Composable
private fun ContentView(
    header: CharacterHeaderUiState,
    onDamageClick: () -> Unit,
    onHealClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                HpActionButton(
                    onClick = { onDamageClick() },
                    iconRes = R.drawable.sword,
                    contentDescription = "Damage",
                )
            }
            // Give HP a bit more visual dominance
            D20HpBar(
                currentHp = header.hitPoints.current,
                maxHp = header.hitPoints.max,
                modifier = Modifier.weight(1.4f),
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                HpActionButton(
                    onClick = { onHealClick() },
                    iconRes = R.drawable.plus,
                    contentDescription = "Heal",
                )
            }
        }
        // Slightly tighter gap between HP and stats strip
        Spacer(modifier = Modifier.height(12.dp))
        StatsCard(
            ac = header.armorClass,
            initiative = header.initiative,
            speed = header.speed,
        )
    }
}

@Composable
private fun HpActionButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(52.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = HexShape,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp),
        )
    }
}

private val HexShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    val r = min(w, h) / 2f
    val cx = w / 2f
    val cy = h / 2f

    fun vertex(angleDeg: Float): Pair<Float, Float> {
        val rad = Math.toRadians(angleDeg.toDouble())
        val x = cx + r * cos(rad).toFloat()
        val y = cy + r * sin(rad).toFloat()
        return x to y
    }

    repeat(6) {
        val (x, y) = vertex(-90f + it * 60f)
        if (it == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
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
