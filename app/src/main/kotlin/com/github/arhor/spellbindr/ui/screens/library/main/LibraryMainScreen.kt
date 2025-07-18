package com.github.arhor.spellbindr.ui.screens.library.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.core.theme.CardBg
import com.github.arhor.spellbindr.core.utils.AppRoute
import com.github.arhor.spellbindr.features.conditions.Conditions
import com.github.arhor.spellbindr.features.spells.Spells

private val ITEMS = listOf(
    Spells to Icons.AutoMirrored.Filled.MenuBook,
    Conditions to Icons.Default.Bookmark,
)

@Composable
fun LibraryMainScreen(
    onItemClick: (AppRoute) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            for ((route, icon) in ITEMS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(12.dp))
                        .background(color = CardBg, shape = RoundedCornerShape(12.dp))
                        .clickable { onItemClick(route) }
                        .padding(vertical = 10.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .background(color = Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                    Text(text = route.title, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}
