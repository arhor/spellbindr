package com.github.arhor.spellbindr.features.spells.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpellSearchInput(
    query: String,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search spell by name") },
        modifier = Modifier.fillMaxWidth(),

        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
            )
        },
        trailingIcon = {
            IconButton(onClick = onFiltersClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Advanced Filters",
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}
