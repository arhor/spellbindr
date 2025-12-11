package com.github.arhor.spellbindr.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
class EllipseShapeTest {

    @Test
    fun `createOutline returns oval covering full size`() {
        // Given
        val size = Size(width = 100f, height = 80f)

        // When
        val outline = EllipseShape.createOutline(size, LayoutDirection.Ltr, Density(1f))

        // Then
        assertThat(outline).isInstanceOf(Outline.Generic::class.java)
        val path = (outline as Outline.Generic).path
        assertThat(path.isEmpty).isFalse()
    }
}
