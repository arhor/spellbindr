package com.github.arhor.spellbindr.ui.components

import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import org.koin.androidx.compose.KoinAndroidContext


@Composable
fun App() {
    SpellbindrTheme {
        KoinAndroidContext {
            AppRouter()
        }
    }
}