package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.theme.AppTheme

private val compendiumSections = listOf(
    AppDestination.CompendiumSpells,
    AppDestination.CompendiumConditions,
    AppDestination.CompendiumAlignments,
    AppDestination.CompendiumRaces,
)

@Composable
internal fun CompendiumScreen(
    onSectionClick: (AppDestination) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(compendiumSections) { section ->
            Card(modifier = Modifier.clickable { onSectionClick(section) }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = section.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CompendiumScreenPreview() {
    AppTheme {
        CompendiumScreen(
            onSectionClick = {},
        )
    }
}
