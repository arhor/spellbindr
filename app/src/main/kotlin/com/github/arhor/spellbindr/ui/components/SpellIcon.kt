package com.github.arhor.spellbindr.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

private val spellIconBitmapCache = ConcurrentHashMap<String, ImageBitmap>()

@Composable
fun SpellIcon(
    spellName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 30.dp,
) {
    val context = LocalContext.current.applicationContext
    val assetName = "icons/spells/${spellName.lowercase().replace(" ", "_")}.png"
    val isInInspectionMode = LocalInspectionMode.current
    val inspectionBitmap = remember(assetName, isInInspectionMode) {
        if (isInInspectionMode) {
            loadSpellIcon(context, assetName)
        } else {
            null
        }
    }
    val bitmap by produceState<ImageBitmap?>(
        initialValue = inspectionBitmap,
        key1 = assetName,
        key2 = isInInspectionMode,
    ) {
        if (inspectionBitmap == null) {
            value = withContext(Dispatchers.IO) {
                loadSpellIcon(context, assetName)
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(color = Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        val resolvedBitmap = bitmap
        if (resolvedBitmap != null) {
            Image(
                bitmap = resolvedBitmap,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

private fun loadSpellIcon(
    context: Context,
    assetName: String,
): ImageBitmap? {
    return spellIconBitmapCache[assetName] ?: runCatching {
        context.assets.open(assetName).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }.getOrNull()?.also { decoded ->
        spellIconBitmapCache[assetName] = decoded
    }
}

@PreviewLightDark
@Composable
private fun SpellIconPreview() {
    AppTheme {
        SpellIcon(spellName = "Fire Bolt")
    }
}
