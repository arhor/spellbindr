package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.ui.theme.RoundedHexShape

data class AbilityScore(
    val name: String,
    val value: Int,
    val bonus: Int,
)

@Composable
fun AbilityScoreCardV2(
    ability: AbilityScore,
    modifier: Modifier = Modifier,
    cardSize: Dp = 72.dp,
) {
    val bonusText = if (ability.bonus >= 0) "+${ability.bonus}" else ability.bonus.toString()
    4.dp

    Box(
        modifier = modifier
            .width(cardSize)
            .height(cardSize),
        contentAlignment = Alignment.TopCenter,
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(cardSize)
                .height(cardSize),
            shape = RoundedHexShape(0.05f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = ability.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = ability.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 5.dp,
            shadowElevation = 3.dp,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 13.dp),
                text = bonusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AbilityScoreCardV2Preview() {
    AppTheme {
        AbilityScoreCardV2(
            ability = AbilityScore(
                name = "CON",
                value = 13,
                bonus = 1,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
