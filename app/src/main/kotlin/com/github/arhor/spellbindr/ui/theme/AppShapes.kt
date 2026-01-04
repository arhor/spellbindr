package com.github.arhor.spellbindr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object EllipseShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rect = size.toRect()
        val path = Path().apply {
            addOval(rect)
            close()
        }
        return Outline.Generic(path)
    }
}

data class ConvexSidesCardShape(val convexityFactor: Float = 0.35f) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height
        val equator = height / 2f
        val arcOffset = minOf(width, height) * convexityFactor

        return Outline.Generic(
            path = Path().apply {
                // start - top left corner
                moveTo(x = 0f, y = 0f)

                // top left to top right line
                lineTo(x = width, y = 0f)

                // right arc
                quadraticTo(
                    x1 = width + arcOffset, y1 = equator,
                    x2 = width, y2 = height,
                )
                // bottom right to bottom left line
                lineTo(x = 0f, y = height)

                // left arc
                quadraticTo(
                    x1 = -arcOffset, y1 = equator,
                    x2 = 0f, y2 = 0f,
                )
                close()
            }
        )
    }
}

data object HexShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val hex = Hex(size)
        val path = Path()

        hex.vertices.forEachIndexed { index, (x, y) ->
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

data class RoundedHexShape(
    val cornerRadiusFraction: Float,
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val hex = Hex(size)
        val cornerRadiusPx = hex.diameter * cornerRadiusFraction
        val roundedPath = buildRoundedPolygonPath(hex.vertices, cornerRadiusPx)

        return Outline.Generic(roundedPath)
    }
}

fun buildRoundedPolygonPath(points: List<Offset>, cornerRadius: Float): Path {
    val path = Path()
    val count = points.size
    for (index in points.indices) {
        val previous = points[(index - 1 + count) % count]
        val current = points[index]
        val next = points[(index + 1) % count]

        val previousVector = current - previous
        val nextVector = next - current

        val previousLength = previousVector.getDistance()
        val nextLength = nextVector.getDistance()

        if (previousLength == 0f || nextLength == 0f) {
            continue
        }

        val limitedRadius = min(cornerRadius, min(previousLength, nextLength) / 2f)
        val previousDirectionX = previousVector.x / previousLength
        val previousDirectionY = previousVector.y / previousLength
        val nextDirectionX = nextVector.x / nextLength
        val nextDirectionY = nextVector.y / nextLength

        val start = Offset(
            x = current.x - previousDirectionX * limitedRadius,
            y = current.y - previousDirectionY * limitedRadius,
        )
        val end = Offset(
            x = current.x + nextDirectionX * limitedRadius,
            y = current.y + nextDirectionY * limitedRadius,
        )

        if (index == 0) {
            path.moveTo(start.x, start.y)
        } else {
            path.lineTo(start.x, start.y)
        }
        path.quadraticTo(current.x, current.y, end.x, end.y)
    }
    path.close()
    return path
}

private data class Hex(
    val diameter: Float,
    val vertices: List<Offset>,
) {
    companion object {
        operator fun invoke(size: Size, inset: Float = 0f): Hex {
            val w = size.width
            val h = size.height
            val d = min(w, h) - inset * 2f
            val r = d / 2f
            val cx = w / 2f
            val cy = h / 2f

            val vertices = List(6) { i ->
                val rad = (-90.0 + i * 60.0) * PI / 180.0
                val x = cx + r * cos(rad).toFloat()
                val y = cy + r * sin(rad).toFloat()
                Offset(x, y)
            }
            return Hex(diameter = d, vertices = vertices)
        }
    }
}
