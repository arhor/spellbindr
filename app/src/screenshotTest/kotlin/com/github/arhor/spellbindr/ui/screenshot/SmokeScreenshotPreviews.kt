package com.github.arhor.spellbindr.ui.screenshot

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.github.arhor.spellbindr.ui.components.AppTopBar
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator

@PreviewTest
@PreviewLightDark
@Composable
fun AppTopBar_Screenshot() {
    ScreenshotHarness {
        AppTopBar(
            config = AppTopBarConfig(
                title = "Spellbindr",
                navigation = AppTopBarNavigation.Back {},
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null,
                        )
                    }
                },
            ),
        )
    }
}

@PreviewTest
@Preview
@Composable
fun ErrorMessage_Screenshot() {
    ScreenshotHarness {
        ErrorMessage("Something went wrong")
    }
}

@PreviewTest
@Preview
@Composable
fun LoadingIndicator_Screenshot() {
    ScreenshotHarness {
        LoadingIndicator()
    }
}
