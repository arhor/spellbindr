package com.github.arhor.spellbindr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.arhor.spellbindr.ui.components.App
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpellbindrTheme {
                KoinAndroidContext {
                    App()
                }
            }
        }
    }
}
