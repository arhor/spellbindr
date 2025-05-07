package com.github.arhor.spellbindr.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun SpellbindrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = determineColorScheme(
            dark = darkTheme,
            dynamic = dynamicColor,
            ctx = LocalContext.current,
        ),
        typography = Typography,
        content = content,
    )
}

private fun determineColorScheme(dark: Boolean, dynamic: Boolean, ctx: Context): ColorScheme =
    when {
        dynamic -> if (dark) {
            dynamicDarkColorScheme(ctx)
        } else {
            dynamicLightColorScheme(ctx)
        }

        dark -> {
            DarkColorScheme
        }

        else -> {
            LightColorScheme
        }
    }

