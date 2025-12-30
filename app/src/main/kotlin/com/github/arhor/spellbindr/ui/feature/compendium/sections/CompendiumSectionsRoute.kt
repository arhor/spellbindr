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
import androidx.navigation.NavHostController
import com.github.arhor.spellbindr.ui.navigation.AppDestination

@Composable
fun CompendiumSectionsRoute(
    controller: NavHostController,
) {
    val sections = listOf(
        CompendiumSectionEntry(title = "Spells", destination = AppDestination.CompendiumSpells),
        CompendiumSectionEntry(title = "Conditions", destination = AppDestination.CompendiumConditions),
        CompendiumSectionEntry(title = "Alignments", destination = AppDestination.CompendiumAlignments),
        CompendiumSectionEntry(title = "Races", destination = AppDestination.CompendiumRaces),
        CompendiumSectionEntry(title = "Traits", destination = AppDestination.CompendiumTraits),
        CompendiumSectionEntry(title = "Features", destination = AppDestination.CompendiumFeatures),
        CompendiumSectionEntry(title = "Classes", destination = AppDestination.CompendiumClasses),
        CompendiumSectionEntry(title = "Equipment", destination = AppDestination.CompendiumEquipment),
    )

    CompendiumSectionsScreen(
        sections = sections,
        onSectionClick = { controller.navigate(it.destination) },
    )
}

@Composable
private fun CompendiumSectionsScreen(
    sections: List<CompendiumSectionEntry>,
    onSectionClick: (CompendiumSectionEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(sections) { section ->
            Card(
                modifier = Modifier.clickable { onSectionClick(section) },
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
    val destination: AppDestination,
)
