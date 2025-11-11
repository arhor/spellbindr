package com.github.arhor.spellbindr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun <T> SelectableGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    smallContent: @Composable (T) -> Unit,
    largeContent: @Composable (T) -> Unit,
) {
    var selectedItem by remember { mutableStateOf<T?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                GridItem(
                    item = item,
                    onItemClick = { selectedItem = it },
                    content = smallContent,
                )
            }
        }

        AnimatedVisibility(
            visible = selectedItem != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selectedItem = null },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                selectedItem?.let {
                    EnlargedItem(item = it, largeContent)
                }
            }
        }
    }
}

@Composable
private fun <T> GridItem(
    item: T,
    onItemClick: (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content(item)
    }
}

@Composable
private fun <T> EnlargedItem(item: T, content: @Composable ((T) -> Unit)) {
    Card(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.large,
    ) {
        content(item)
    }
}

@Preview(showBackground = true)
@Composable
fun SelectableGridPreview() {
    AppTheme {
        SelectableGrid(
            items = List(9) { index -> "Item ${index + 1}" to "Description for item ${index + 1}" },
            smallContent = { (title, _) ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            },
            largeContent = { (title, description) ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            },
        )
    }
}
