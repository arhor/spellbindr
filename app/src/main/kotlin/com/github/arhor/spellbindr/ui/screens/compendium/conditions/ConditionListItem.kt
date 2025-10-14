package com.github.arhor.spellbindr.ui.screens.compendium.conditions

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.ui.components.GradientDivider

@Composable
fun ConditionListItem(
    condition: Condition,
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
                text = condition.displayName,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    GradientDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = condition.description.joinToString(separator = "\n\n"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
