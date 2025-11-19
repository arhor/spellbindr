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
import androidx.compose.ui.geometry.lerp
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
import com.github.arhor.spellbindr.ui.theme.buildRoundedPolygonPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A reusable d20-inspired HP indicator that renders a hexagonal meter whose fill depletes from top
 * to bottom. It is intended for the Spellbindr character sheet vitals section but can be reused in
 * any composable that needs to visualize hit points.
 */
@Composable
fun D20HpBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier,
) {
    val sanitizedMax = max(maxHp, 0)
    val sanitizedCurrent = currentHp.coerceIn(0, sanitizedMax)
    val hpFraction = if (sanitizedMax > 0) {
        sanitizedCurrent.toFloat() / sanitizedMax
    } else {
        0f
    }

    val hexBackgroundColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val facetColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val textColor = if (hpFraction > 0.45f) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val color = Color(red = 179, green = 38, blue = 30)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val projection = D20Projection.create(size)
            val polygonPath = buildRoundedPolygonPath(
                points = projection.outerVertices,
                cornerRadius = size.minDimension * 0.03f,
            )

            clipPath(polygonPath) {
                // 1) Always draw a "base" faceted die for empty HP
                drawFacetedHexFill(
                    projection = projection,
                    baseColor = lerp(hexBackgroundColor, color, 0.10f),
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
                            projection = projection,
                            baseColor = color,
                        )
                    }
                }

                // 3) Lines on top
                drawFacetLines(
                    projection = projection,
                    color = facetColor,
                    strokeWidth = size.minDimension * 0.01f,
                )
            }

            drawPath(
                path = polygonPath,
                color = outlineColor,
                style = Stroke(width = size.minDimension * 0.015f),
            )
        }

        Text(
            text = "$sanitizedCurrent / $sanitizedMax",
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            textAlign = TextAlign.Center,
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

private fun DrawScope.drawFacetedHexFill(
    projection: D20Projection,
    baseColor: Color,
) {
    // Light coming from top-left
    val lightDir = Offset(-1f, -1f).let { dir ->
        val len = sqrt(dir.x * dir.x + dir.y * dir.y)
        Offset(dir.x / len, dir.y / len)
    }

    val lightColor = lerp(baseColor, Color.White, 0.35f)
    val darkColor = lerp(baseColor, Color.Black, 0.35f)

    val expansionFactor = 1.003f // try 1.01–1.05 and tweak visually

    fun expandFromCentroid(p: Offset, centroid: Offset): Offset {
        val vx = p.x - centroid.x
        val vy = p.y - centroid.y
        return Offset(
            x = centroid.x + vx * expansionFactor,
            y = centroid.y + vy * expansionFactor,
        )
    }

    for (facet in projection.facets) {
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
        val v = Offset(cx - projection.center.x, cy - center.y)
        val vLen = sqrt(v.x * v.x + v.y * v.y)
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

private fun DrawScope.drawFacetLines(
    projection: D20Projection,
    color: Color,
    strokeWidth: Float,
) {
    val path = Path().apply {
        projection.edges.forEach { (start, end) ->
            moveTo(start.x, start.y)
            lineTo(end.x, end.y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private data class D20Projection(
    val top: Offset,
    val topRight: Offset,
    val bottomRight: Offset,
    val bottom: Offset,
    val bottomLeft: Offset,
    val topLeft: Offset,
    val topInner: Offset,
    val rightInner: Offset,
    val leftInner: Offset,
    val center: Offset,
) {
    val outerVertices: List<Offset> = listOf(
        top,
        topRight,
        bottomRight,
        bottom,
        bottomLeft,
        topLeft,
    )
    val edges: List<Edge> = listOf(
        Edge(top, topInner),
        Edge(topInner, topLeft),
        Edge(topInner, topRight),
        Edge(topInner, leftInner),
        Edge(topInner, rightInner),
        Edge(leftInner, rightInner),
        Edge(bottom, leftInner),
        Edge(bottom, rightInner),
        Edge(topLeft, leftInner),
        Edge(bottomLeft, leftInner),
        Edge(topRight, rightInner),
        Edge(bottomRight, rightInner),
    )
    val facets: List<Facet> = listOf(
        Facet(top, topLeft, topInner),
        Facet(top, topRight, topInner),
        Facet(topLeft, topInner, leftInner),
        Facet(topRight, rightInner, topInner),
        Facet(topInner, leftInner, rightInner),
        Facet(bottom, leftInner, rightInner),
        Facet(topLeft, leftInner, bottomLeft),
        Facet(bottomLeft, leftInner, bottom),
        Facet(topRight, bottomRight, rightInner),
        Facet(bottomRight, bottom, rightInner),
    )

    companion object {
        fun create(size: Size): D20Projection {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val startAngle = -90f
            val step = 60f

            fun vertexOffset(index: Int): Offset {
                val angle = (startAngle + step * index) * (PI / 180f)
                return Offset(
                    x = center.x + radius * cos(angle).toFloat(),
                    y = center.y + radius * sin(angle).toFloat(),
                )
            }

            val top = vertexOffset(index = 0)
            val topRight = vertexOffset(index = 1)
            val bottomRight = vertexOffset(index = 2)
            val bottom = vertexOffset(index = 3)
            val bottomLeft = vertexOffset(index = 4)
            val topLeft = vertexOffset(index = 5)

            val width = topRight.x - topLeft.x
            val baseY = center.y + (bottom.y - center.y) * 0.33f
            val offsetX = width * 0.31f

            val topInner = lerp(top, center, 0.34f)
            val rightInner = Offset(center.x + offsetX, baseY)
            val leftInner = Offset(center.x - offsetX, baseY)

            return D20Projection(
                top = top,
                topRight = topRight,
                bottomRight = bottomRight,
                bottom = bottom,
                bottomLeft = bottomLeft,
                topLeft = topLeft,
                topInner = topInner,
                rightInner = rightInner,
                leftInner = leftInner,
                center = center,
            )
        }
    }
}

private data class Edge(
    val a: Offset,
    val b: Offset,
)

private data class Facet(
    val a: Offset,
    val b: Offset,
    val c: Offset,
)
