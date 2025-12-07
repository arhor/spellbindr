package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun WeaponsTab(
    weapons: WeaponsTabState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WeaponsCard(weapons = weapons.weapons)
    }
}

@Preview(showBackground = true)
@Composable
private fun WeaponsTabLightPreview() {
    WeaponsTabPreview(isDarkTheme = false)
}

@Preview(showBackground = true)
@Composable
private fun WeaponsTabDarkPreview() {
    WeaponsTabPreview(isDarkTheme = true)
}

@Composable
private fun WeaponsTabPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        WeaponsTab(weapons = CharacterSheetPreviewData.weapons)
    }
}
