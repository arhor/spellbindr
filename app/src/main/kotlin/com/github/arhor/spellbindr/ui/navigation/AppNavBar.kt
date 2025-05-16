package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

private val NAV_ITEMS = listOf(
    AppRoute.SpellSearch,
    AppRoute.FavoriteSpells,
    AppRoute.Characters,
)

@Composable
fun AppNavBar(
    onItemClick: (AppRoute) -> Unit,
    isItemSelected: (AppRoute) -> Boolean,
) {
    NavigationBar {
        for (item in NAV_ITEMS) {
            NavigationBarItem(
                selected = isItemSelected(item),
                onClick = { onItemClick(item) },
                label = { Text(item.title) },
                icon = { }
            )
        }
    }
}
