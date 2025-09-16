package com.github.arhor.spellbindr.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme

@Composable
internal fun PreviewScope(content: @Composable () -> Unit) {
    SpellbindrTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
