package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * A reusable d20-inspired HP indicator that renders a hexagonal meter whose fill depletes from top
 * to bottom. It is intended for the Spellbindr character sheet vitals section but can be reused in
 * any composable that needs to visualize hit points.
 */
@Composable
fun D20HpBar(
    currentHp: Int,
    maxHp: Int,
) {
    val sanitizedMax = max(maxHp, 0)
    val sanitizedCurrent = currentHp.coerceIn(0, sanitizedMax)
    val hpFraction = if (sanitizedMax > 0) {
        sanitizedCurrent.toFloat() / sanitizedMax
    } else {
        0f
    }

    val hexBackgroundColor = MaterialTheme.colorScheme.surface
    val fillColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline
    val facetColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val textColor = if (hpFraction > 0.45f) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val hexPoints = createHexagonPoints(size)
            val hexPath = buildRoundedPolygonPath(hexPoints, cornerRadius = size.minDimension * 0.03f)

            clipPath(hexPath) {
                // Base background inside hex
                // (optional: keep or remove, facets will cover it anyway)
                // drawRect(color = hexBackgroundColor)

                // 1) Always draw a "base" faceted die for empty HP
                val emptyColor = lerp(hexBackgroundColor, fillColor, 0.10f)
                drawFacetedHexFill(
                    points = hexPoints,
                    baseColor = emptyColor,
                )

                // 2) Overlay faceted fill only for the HP portion
                val fillHeight = size.height * hpFraction
                if (fillHeight > 0f) {
                    val fillTop = size.height - fillHeight

                    clipRect(
                        left = 0f,
                        top = fillTop,
                        right = size.width,
                        bottom = size.height,
                    ) {
                        drawFacetedHexFill(
                            points = hexPoints,
                            baseColor = fillColor,
                        )
                    }
                }

                // 3) Lines on top
                drawFacetLines(
                    points = hexPoints,
                    color = facetColor,
                    strokeWidth = size.minDimension * 0.01f,
                )
            }

            drawPath(
                path = hexPath,
                color = outlineColor,
                style = Stroke(width = size.minDimension * 0.025f),
            )
        }


        Text(
            text = "$sanitizedCurrent / $sanitizedMax",
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

private fun createHexagonPoints(size: Size): List<Offset> {
    val radius = size.minDimension / 2f
    val center = Offset(x = size.width / 2f, y = size.height / 2f)
    val startAngle = -90f
    val step = 60f

    return List(6) { index ->
        val angle = (startAngle + step * index) * (PI / 180f)
        Offset(
            x = center.x + radius * cos(angle).toFloat(),
            y = center.y + radius * sin(angle).toFloat(),
        )
    }
}

private fun buildRoundedPolygonPath(points: List<Offset>, cornerRadius: Float): Path {
    val path = Path()
    if (points.size < 3) {
        return path
    }
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

private fun DrawScope.drawFacetLines(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 6) {
        return
    }

    // Outer hex vertices (matching createHexagonPoints order)
    val top = points[0]
    val topRight = points[1]
    val bottomRight = points[2]
    val bottom = points[3]
    val bottomLeft = points[4]
    val topLeft = points[5]

    val center = Offset(size.width / 2f, size.height / 2f)

    fun lerp(start: Offset, stop: Offset, fraction: Float): Offset =
        Offset(
            x = start.x + (stop.x - start.x) * fraction,
            y = start.y + (stop.y - start.y) * fraction,
        )

    // Inner “axis” points (top of 20, bottom of 8)
    val topInner = lerp(top, center, 0.40f)

    // Horizontal “equator” for the base of 20 / top of 8
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val width = maxX - minX
    val baseY = center.y + (bottom.y - center.y) * 0.30f
    val offsetX = width * 0.28f

    val leftInner = Offset(center.x - offsetX, baseY)
    val rightInner = Offset(center.x + offsetX, baseY)

    val edges = listOf(
        // Top wedge (face 18 / 4 area)
        top to topInner,
        topInner to topLeft,
        topInner to topRight,

        // Central 20 triangle
        topInner to leftInner,
        topInner to rightInner,
        leftInner to rightInner,

        // 8-triangle connections
        bottom to leftInner,
        bottom to rightInner,

        // Left side faces (2 / 12 / 10)
        topLeft to leftInner,
        bottomLeft to leftInner,

        // Right side faces (14 / 6 / 16)
        topRight to rightInner,
        bottomRight to rightInner,
    )

    val path = Path().apply {
        edges.forEach { (start, end) ->
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,   // or Butt if you want sharp ends
            join = StrokeJoin.Round, // nicer at intersections
        ),
    )
}

private fun DrawScope.drawFacetedHexFill(
    points: List<Offset>,
    baseColor: Color,
) {
    if (points.size < 6) {
        return
    }

    // Same naming & geometry as in drawFacetLines
    val top = points[0]
    val topRight = points[1]
    val bottomRight = points[2]
    val bottom = points[3]
    val bottomLeft = points[4]
    val topLeft = points[5]

    val center = Offset(size.width / 2f, size.height / 2f)

    fun lerpOffset(start: Offset, stop: Offset, fraction: Float): Offset =
        Offset(
            x = start.x + (stop.x - start.x) * fraction,
            y = start.y + (stop.y - start.y) * fraction,
        )

    // Inner “axis” points – keep factors in sync with drawFacetLines
    val topInner = lerpOffset(top, center, 0.40f)

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val width = maxX - minX
    val baseY = center.y + (bottom.y - center.y) * 0.30f
    val offsetX = width * 0.28f

    val leftInner = Offset(center.x - offsetX, baseY)
    val rightInner = Offset(center.x + offsetX, baseY)

    data class Facet(val a: Offset, val b: Offset, val c: Offset)

    val facets = listOf(
        // Top cap (split into two triangles)
        Facet(top, topLeft, topInner),
        Facet(top, topInner, topRight),

        // Upper side facets
        Facet(topLeft, topInner, leftInner),
        Facet(topRight, rightInner, topInner),

        // Central 20-face triangle
        Facet(topInner, leftInner, rightInner),

        // Central 8-face triangle
        Facet(bottom, leftInner, rightInner),

        // Left stack below
        Facet(topLeft, leftInner, bottomLeft),
        Facet(bottomLeft, leftInner, bottom),

        // Right stack below
        Facet(topRight, bottomRight, rightInner),
        Facet(bottomRight, bottom, rightInner),
    )

    // Light coming from top-left
    val lightDir = Offset(-1f, -1f).let { dir ->
        val len = kotlin.math.sqrt(dir.x * dir.x + dir.y * dir.y)
        Offset(dir.x / len, dir.y / len)
    }

    val lightColor = lerp(baseColor, Color.White, 0.35f)
    val darkColor = lerp(baseColor, Color.Black, 0.35f)

    // >>> This is the key bit for Option 2 <<<
    val expansionFactor = 1.003f // try 1.01–1.05 and tweak visually

    fun expandFromCentroid(p: Offset, centroid: Offset): Offset {
        val vx = p.x - centroid.x
        val vy = p.y - centroid.y
        return Offset(
            x = centroid.x + vx * expansionFactor,
            y = centroid.y + vy * expansionFactor,
        )
    }

    facets.forEach { facet ->
        // Centroid of the (original) triangle
        val originalCentroid = Offset(
            x = (facet.a.x + facet.b.x + facet.c.x) / 3f,
            y = (facet.a.y + facet.b.y + facet.c.y) / 3f,
        )

        // Expand vertices slightly away from the facet centroid
        val a = expandFromCentroid(facet.a, originalCentroid)
        val b = expandFromCentroid(facet.b, originalCentroid)
        val c = expandFromCentroid(facet.c, originalCentroid)

        // Recompute centroid for shading using expanded vertices
        val cx = (a.x + b.x + c.x) / 3f
        val cy = (a.y + b.y + c.y) / 3f
        val v = Offset(cx - center.x, cy - center.y)
        val vLen = kotlin.math.sqrt(v.x * v.x + v.y * v.y)
        val vNorm = if (vLen > 0f) Offset(v.x / vLen, v.y / vLen) else Offset.Zero

        val dot = vNorm.x * lightDir.x + vNorm.y * lightDir.y
        val t = ((dot + 1f) / 2f).coerceIn(0f, 1f)
        val facetColor = lerp(darkColor, lightColor, t)

        val path = Path().apply {
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        }

        drawPath(
            path = path,
            color = facetColor,
        )
    }
}


@Preview(name = "HP Full")
@Composable
private fun D20HpBarFullPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            D20HpBar(currentHp = 38, maxHp = 38)
        }
    }
}

@Preview(name = "HP Mid")
@Composable
private fun D20HpBarMidPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            D20HpBar(currentHp = 20, maxHp = 38)
        }
    }
}

@Preview(name = "HP Low")
@Composable
private fun D20HpBarLowPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            D20HpBar(currentHp = 4, maxHp = 38)
        }
    }
}
