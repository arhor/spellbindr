package com.github.arhor.spellbindr.ui.screens.library.conditions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.Condition
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.ui.theme.CardBg

@Composable
fun ConditionsScreen(
    viewModel: ConditionSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var expandedItemName by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.conditions,
            key = { it.name }
        ) { condition ->
            ConditionListItem(
                condition = condition,
                isExpanded = condition.name == expandedItemName,
                onItemClick = {
                    expandedItemName = if (condition.name == expandedItemName) {
                        null
                    } else {
                        condition.name
                    }
                },
            )
        }
    }
}

@Composable
private fun ConditionListItem(
    condition: Condition,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.border(
            width = 1.dp,
            shape = RoundedCornerShape(16.dp),
            brush = Brush.linearGradient(
                colors = listOf(
                    Accent.copy(alpha = 0.2f),
                    Accent,
                    Accent.copy(alpha = 0.2f),
                ),
            )
        ),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        onClick = onItemClick,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
        ) {
            Text(
                text = condition.name,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    GradientDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = condition.desc.joinToString(separator = "\n\n"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
} 
