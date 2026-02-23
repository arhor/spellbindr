package com.github.arhor.spellbindr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
        ),
        headlineSmall = headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        ),
        titleLarge = titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
        ),
        titleMedium = titleMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.25.sp,
        ),
        bodyLarge = bodyLarge.copy(
            lineHeight = 22.sp,
        ),
        bodyMedium = bodyMedium.copy(
            lineHeight = 20.sp,
        ),
        bodySmall = bodySmall.copy(
            lineHeight = 18.sp,
        ),
        labelMedium = labelMedium.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = labelSmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        ),
    )
}
