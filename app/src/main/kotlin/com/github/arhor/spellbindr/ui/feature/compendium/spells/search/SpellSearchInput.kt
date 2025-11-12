package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpellSearchInput(
    query: String,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    showFavorite: Boolean,
    onFavoriteClick: () -> Unit,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onFiltersClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Advanced Filters",
                    )
                }
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (showFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (showFavorite) "Favorites: ON" else "Favorites: OFF",
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}
