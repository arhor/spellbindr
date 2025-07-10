package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

private val NAV_ITEMS = listOf(
    AppRoute.Library,
    AppRoute.Characters,
)

@Composable
fun AppNavBar(
    onItemClick: (AppRoute) -> Unit,
    isItemSelected: (AppRoute) -> Boolean,
) {
    NavigationBar {
        NAV_ITEMS.forEach {
            NavigationBarItem(
                selected = isItemSelected(it),
                onClick = { onItemClick(it) },
                label = { Text(it.title) },
                icon = { }
            )
        }
    }
}
