package com.github.arhor.spellbindr.ui.feature.character.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem

@Composable
fun CharacterCard(
    item: CharacterListItem,
    onClick: () -> Unit,
) {
    val displayName by remember(item.name) {
        derivedStateOf { item.name.ifBlank { "Unnamed hero" } }
    }
    val headline by remember(item.level, item.className) {
        derivedStateOf {
            buildString {
                append("Level ${item.level.coerceAtLeast(1)}")
                if (item.className.isNotBlank()) {
                    append(' ')
                    append(item.className)
                }
            }
        }
    }
    val detail by remember(item.race, item.background) {
        derivedStateOf {
            listOfNotNull(
                item.race.takeIf { it.isNotBlank() },
                item.background.takeIf { it.isNotBlank() },
            ).joinToString(separator = " • ")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
            )
            if (headline.isNotBlank()) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
