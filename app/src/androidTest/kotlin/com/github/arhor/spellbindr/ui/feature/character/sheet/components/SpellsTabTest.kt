package com.github.arhor.spellbindr.ui.feature.character.sheet.components

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.SpellsTabPreview
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class SpellsTabTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun savesComposableScreenshot() {
        composeRule.setContent {
            SpellsTabPreview()
        }

        composeRule.waitForIdle()

        val bitmap = composeRule.onRoot()
            .captureToImage()
            .asAndroidBitmap()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val file = File(dir, "SpellsTabPreview.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
