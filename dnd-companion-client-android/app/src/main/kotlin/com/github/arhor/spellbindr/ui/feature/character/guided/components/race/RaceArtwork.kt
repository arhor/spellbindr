package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads optional race artwork and falls back to a deterministic placeholder. The race mechanics and name are
 * deliberately rendered elsewhere; this composable is decorative and therefore exposes no content description.
 */
@Composable
internal fun RaceArtwork(
    raceId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val assetName = "images/races/$raceId.webp"
    val bitmap by produceState<ImageBitmap?>(initialValue = raceArtworkCache[assetName], key1 = assetName) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(assetName).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }?.also { decoded -> raceArtworkCache[assetName] = decoded }
        }
    }
    val colors = remember(raceId) { placeholderPalette(raceId) }
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(colors.backgroundTop, colors.backgroundBottom),
            ),
        ),
    ) {
        val resolvedBitmap = bitmap
        if (resolvedBitmap != null) {
            Image(
                bitmap = resolvedBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = colors.glow.copy(alpha = 0.32f),
                    radius = size.minDimension * 0.38f,
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                )
                drawRepresentative(
                    centerX = size.width * 0.35f,
                    baselineY = size.height * 1.04f,
                    height = size.height * 0.76f,
                    color = colors.figureOne.copy(alpha = 0.80f),
                    tiltDegrees = -4f,
                )
                drawRepresentative(
                    centerX = size.width * 0.66f,
                    baselineY = size.height * 1.02f,
                    height = size.height * 0.82f,
                    color = colors.figureTwo.copy(alpha = 0.78f),
                    tiltDegrees = 4f,
                )
            }
        }
    }
}

private fun DrawScope.drawRepresentative(
    centerX: Float,
    baselineY: Float,
    height: Float,
    color: Color,
    tiltDegrees: Float,
) {
    rotate(degrees = tiltDegrees, pivot = Offset(centerX, baselineY)) {
        val headRadius = height * 0.095f
        val headCenter = Offset(centerX, baselineY - height + headRadius)
        drawCircle(color = color, radius = headRadius, center = headCenter)

        val shouldersY = headCenter.y + headRadius * 1.35f
        val silhouette = Path().apply {
            moveTo(centerX - height * 0.18f, shouldersY + height * 0.08f)
            quadraticTo(centerX, shouldersY - height * 0.04f, centerX + height * 0.18f, shouldersY + height * 0.08f)
            lineTo(centerX + height * 0.27f, baselineY)
            lineTo(centerX - height * 0.27f, baselineY)
            close()
        }
        drawPath(path = silhouette, color = color)
        drawOval(
            color = color.copy(alpha = 0.22f),
            topLeft = Offset(centerX - height * 0.34f, baselineY - height * 0.10f),
            size = Size(height * 0.68f, height * 0.13f),
        )
    }
}

private data class PlaceholderPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val glow: Color,
    val figureOne: Color,
    val figureTwo: Color,
)

private val raceArtworkCache = ConcurrentHashMap<String, ImageBitmap>()

private fun placeholderPalette(raceId: String): PlaceholderPalette {
    val palettes = listOf(
        PlaceholderPalette(
            Color(0xFF17324D),
            Color(0xFF081A2C),
            Color(0xFF6EC6D9),
            Color(0xFFD5A979),
            Color(0xFF89A8B2),
        ),
        PlaceholderPalette(
            Color(0xFF3E2A4E),
            Color(0xFF171025),
            Color(0xFFD4A5FF),
            Color(0xFFCFA783),
            Color(0xFF8EA6B4),
        ),
        PlaceholderPalette(
            Color(0xFF374629),
            Color(0xFF15200F),
            Color(0xFFC6D989),
            Color(0xFFD9B18C),
            Color(0xFF91A99C),
        ),
        PlaceholderPalette(
            Color(0xFF563125),
            Color(0xFF24100B),
            Color(0xFFFFB36A),
            Color(0xFFD8A47A),
            Color(0xFF91A3AE),
        ),
    )
    return palettes[(raceId.hashCode() and Int.MAX_VALUE) % palettes.size]
}
