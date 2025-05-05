package com.github.arhor.spellbindr.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppNavBar(
    items: Iterable<Pair<String, String>>,
    onItemClick: (String) -> Unit,
    isItemSelected: (String) -> Boolean,
) {
    NavigationBar {
        for ((route, label) in items) {
            NavigationBarItem(
                selected = isItemSelected(route),
                onClick = { onItemClick(route) },
                label = { Text(label) },
                icon = { }
            )
        }
    }
}
