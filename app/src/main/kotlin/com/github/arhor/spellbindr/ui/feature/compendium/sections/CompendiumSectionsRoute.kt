@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.compendium.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun CompendiumSectionsRoute(
    onNavigateToSpells: () -> Unit,
    onNavigateToConditions: () -> Unit,
    onNavigateToAlignments: () -> Unit,
    onNavigateToRaces: () -> Unit,
    onNavigateToTraits: () -> Unit,
    onNavigateToFeatures: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToEquipment: () -> Unit,
) {
    val sections = listOf(
        CompendiumSectionEntry(title = "Spells", onClick = onNavigateToSpells),
        CompendiumSectionEntry(title = "Conditions", onClick = onNavigateToConditions),
        CompendiumSectionEntry(title = "Alignments", onClick = onNavigateToAlignments),
        CompendiumSectionEntry(title = "Races", onClick = onNavigateToRaces),
        CompendiumSectionEntry(title = "Traits", onClick = onNavigateToTraits),
        CompendiumSectionEntry(title = "Features", onClick = onNavigateToFeatures),
        CompendiumSectionEntry(title = "Classes", onClick = onNavigateToClasses),
        CompendiumSectionEntry(title = "Equipment", onClick = onNavigateToEquipment),
    )

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Compendium") },
            ),
        ),
    ) {
        CompendiumSectionsScreen(sections = sections)
    }
}

@Composable
private fun CompendiumSectionsScreen(sections: List<CompendiumSectionEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(sections) { section ->
            Card(
                modifier = Modifier.clickable(onClick = section.onClick),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = section.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                    section.description?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private data class CompendiumSectionEntry(
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit,
)
