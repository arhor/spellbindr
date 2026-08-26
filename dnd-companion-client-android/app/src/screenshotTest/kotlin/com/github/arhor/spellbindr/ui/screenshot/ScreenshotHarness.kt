package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun ScreenshotHarness(
    content: @Composable () -> Unit,
) {
    AppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
