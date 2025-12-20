package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.components.GradientDivider

@Composable
fun RaceListItem(
    race: Race,
    traits: Map<String, Trait>,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onItemClick,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
        ) {
            Text(
                text = race.name,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    GradientDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (race.traits.isNotEmpty()) {
                        Text(
                            text = "Traits:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        race.traits.forEach {
                            traits[it.id]?.let { trait ->
                                TraitDisplay(
                                    trait = trait,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }

                    if (race.subraces.isNotEmpty()) {
                        Text(
                            text = "Subraces:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        race.subraces.forEach { subrace ->
                            Text(
                                text = "• ${subrace.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                            )
                            if (subrace.desc.isNotEmpty()) {
                                Text(
                                    text = subrace.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                )
                            }
                            if (subrace.traits.isNotEmpty()) {
                                Text(
                                    text = "Traits:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                                )
                                subrace.traits.forEach {
                                    traits[it.id]?.let { trait ->
                                        TraitDisplay(
                                            trait = trait,
                                            modifier = Modifier.padding(start = 24.dp, bottom = 4.dp),
                                            compact = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitDisplay(
    trait: Trait,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = trait.name,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        trait.desc.forEach { description ->
            Text(
                text = description,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = if (compact) 8.dp else 12.dp)
            )
        }
    }
}
