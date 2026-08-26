package com.github.arhor.spellbindr.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isSplashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen().apply {
            setKeepOnScreenCondition { isSplashVisible }
        }
        super.onCreate(savedInstanceState)
        setContent {
            SpellbindrApp(onReady = { isSplashVisible = false })
        }
    }
}
