package com.github.arhor.spellbindr.ui.feature.compendium.races.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Trait

@Composable
fun TraitDisplay(
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
