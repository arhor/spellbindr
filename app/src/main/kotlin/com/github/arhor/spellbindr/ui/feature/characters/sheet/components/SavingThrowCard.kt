package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.ui.theme.ConvexSidesCardShape
import com.github.arhor.spellbindr.utils.signed

@Composable
fun SavingThrowCard(
    abilityName: String,
    bonus: Int,
    proficient: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = CutCornerShape(9.dp),
            colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(2.dp),
            border = BorderStroke(
                width = 3.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = abilityName,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        SelectedIndicator(
            modifier = Modifier.align(Alignment.CenterStart),
            selected = proficient,
        )
        SavingThrowBonusCard(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(30.dp)
                .height(40.dp),
            bonus = bonus,
        )
    }
}

@Composable
private fun SavingThrowBonusCard(
    modifier: Modifier = Modifier,
    bonus: Int,
) {
    Card(
        modifier = modifier,
        shape = ConvexSidesCardShape(convexityFactor = .55f),
        colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            width = 3.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = signed(bonus),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavingThrowCardLightPreview() {
    SavingThrowCardPreview(isDarkTheme = false)
}

@Preview(showBackground = true)
@Composable
private fun SavingThrowCardDarkPreview() {
    SavingThrowCardPreview(isDarkTheme = true)
}

@Composable
private fun SavingThrowCardPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        SavingThrowCardPreviewContent()
    }
}

@Composable
private fun SavingThrowCardPreviewContent() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val abilities = CharacterSheetPreviewData.overview.abilities
        SavingThrowCard(
            abilityName = abilities[0].label,
            bonus = abilities[0].savingThrowBonus,
            proficient = abilities[0].savingThrowProficient,
        )
        SavingThrowCard(
            abilityName = abilities[1].label,
            bonus = abilities[1].savingThrowBonus,
            proficient = abilities[1].savingThrowProficient,
        )
    }
}
