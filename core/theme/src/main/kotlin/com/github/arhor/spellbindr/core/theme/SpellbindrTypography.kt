package com.github.arhor.spellbindr.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

val SpellbindrTypography = with(receiver = Typography()) {
    copy(
        displayLarge = displayLarge.copy(
            fontFamily = FontFamily.Serif
        ),
        displayMedium = displayMedium.copy(
            fontFamily = FontFamily.Serif,
        ),
        displaySmall = displaySmall.copy(
            fontFamily = FontFamily.Serif
        ),
        headlineLarge = headlineLarge.copy(
            fontFamily = FontFamily.Serif
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.Serif
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = FontFamily.Serif
        ),
        titleLarge = titleLarge.copy(
            fontFamily = FontFamily.Serif
        ),
        titleMedium = titleMedium.copy(
            fontFamily = FontFamily.Serif
        ),
        titleSmall = titleSmall.copy(
            fontFamily = FontFamily.Serif
        ),
        bodyLarge = bodyLarge.copy(
            fontFamily = FontFamily.Serif
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = FontFamily.Serif
        ),
        bodySmall = bodySmall.copy(
            fontFamily = FontFamily.Serif
        ),
        labelLarge = labelLarge.copy(
            fontFamily = FontFamily.Serif
        ),
        labelMedium = labelMedium.copy(
            fontFamily = FontFamily.Serif
        ),
        labelSmall = labelSmall.copy(
            fontFamily = FontFamily.Serif
        ),
    )
}
