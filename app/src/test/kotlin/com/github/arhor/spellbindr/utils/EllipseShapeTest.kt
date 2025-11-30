package com.github.arhor.spellbindr.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EllipseShapeTest {

    @Test
    fun `createOutline returns oval covering full size`() {
        // Given
        val size = Size(width = 100f, height = 80f)

        // When
        val outline = EllipseShape.createOutline(size, LayoutDirection.Ltr, Density(1f))

        // Then
        assertTrue(outline is Outline.Generic)
        val pathBounds = (outline as Outline.Generic).path.getBounds()
        assertEquals(Rect(0f, 0f, size.width, size.height), pathBounds)
    }
}
