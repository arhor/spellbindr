package com.github.arhor.spellbindr.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.navigation.AppRoute

@Composable
fun AppNavBar(
    items: Iterable<AppRoute>,
    onItemClick: (AppRoute) -> Unit,
    isItemSelected: (AppRoute) -> Boolean,
) {
    NavigationBar {
        for (item in items) {
            NavigationBarItem(
                selected = isItemSelected(item),
                onClick = { onItemClick(item) },
                label = { Text(item.title) },
                icon = { }
            )
        }
    }
}
