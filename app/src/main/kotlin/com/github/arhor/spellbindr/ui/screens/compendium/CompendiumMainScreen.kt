package com.github.arhor.spellbindr.ui.screens.compendium

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.navigation.Alignments
import com.github.arhor.spellbindr.ui.navigation.Conditions
import com.github.arhor.spellbindr.ui.navigation.Races
import com.github.arhor.spellbindr.ui.navigation.Spells
import com.github.arhor.spellbindr.utils.AppRoute

private val ITEMS = listOf(
    Spells to Icons.AutoMirrored.Filled.MenuBook,
    Conditions to Icons.Default.Bookmark,
    Alignments to Icons.Default.Balance,
    Races to Icons.Default.People,
)

@Composable
fun CompendiumMainScreen(
    onItemClick: (AppRoute) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            for ((route, icon) in ITEMS) {
                ElevatedCard(
                    onClick = { onItemClick(route) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = route.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
