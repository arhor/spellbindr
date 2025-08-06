package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BaseScreenWithNavigation(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    prevText: String = "Prev",
    nextText: String = "Next",
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Space for the navigation buttons
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(padding),
                content = content,
            )
        }
        
        // Sticky navigation buttons at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            NavButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding, vertical = padding),
                onPrev = onPrev,
                onNext = onNext,
                prevText = prevText,
                nextText = nextText
            )
        }
    }
}
