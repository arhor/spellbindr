package com.github.arhor.spellbindr.ui.feature.compendium.spells.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellSearchInput(
    query: String,
    onQueryChanged: (String) -> Unit,
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
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

@PreviewLightDark
@Composable
private fun SpellSearchInputPreview() {
    AppTheme {
        SpellSearchInput(
            query = "Magic",
            onQueryChanged = {},
            showFavorite = true,
            onFavoriteClick = {},
        )
    }
}
