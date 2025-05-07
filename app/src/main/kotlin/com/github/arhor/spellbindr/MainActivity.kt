package com.github.arhor.spellbindr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.github.arhor.spellbindr.ui.App
import com.github.arhor.spellbindr.ui.AppNavGraph
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isSplashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().also {
            it.setKeepOnScreenCondition {
                isSplashVisible
            }
        }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(1500)
            isSplashVisible = false
        }
        enableEdgeToEdge()
        setContent { App() }
    }
}
