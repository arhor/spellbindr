package com.github.arhor.spellbindr.ui.feature.character.guided.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice

@Composable
internal fun ChoiceSection(
    title: String,
    description: String?,
    choice: Choice,
    selected: Set<String>,
    options: Map<String, String>,
    disabledOptions: Map<String, String> = emptyMap(),
    onToggle: (String) -> Unit,
) {
    val isSingleSelect = choice.choose == 1
    var query by remember(title, options.size) { mutableStateOf("") }
    val showSearch = options.size >= 12
    val filteredOptions = remember(options, query) {
        if (query.isBlank()) {
            options.entries.toList()
        } else {
            options.entries.filter { (_, label) ->
                label.contains(query, ignoreCase = true)
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Selected: ${selected.size} / ${choice.choose}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (disabledOptions.isNotEmpty()) {
                val alreadyHaveCount = options.keys.count { it !in selected && it in disabledOptions }
                if (alreadyHaveCount > 0) {
                    Text(
                        text = "Already have: $alreadyHaveCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (options.isEmpty()) {
                Text(
                    text = "No options available (MVP limitation).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search options") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            if (filteredOptions.isEmpty()) {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            val useLazyOptions = filteredOptions.size >= 30

            if (useLazyOptions) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                ) {
                    items(filteredOptions, key = { it.key }) { (id, label) ->
                        OptionRow(
                            id = id,
                            label = label,
                            disabledOptions = disabledOptions,
                            selected = selected,
                            isSingleSelect = isSingleSelect,
                            choice = choice,
                            onToggle = onToggle,
                        )
                    }
                }
            } else {
                filteredOptions.forEach { (id, label) ->
                    OptionRow(
                        id = id,
                        label = label,
                        disabledOptions = disabledOptions,
                        selected = selected,
                        isSingleSelect = isSingleSelect,
                        choice = choice,
                        onToggle = onToggle
                    )
                }
            }
        }
    }
}
