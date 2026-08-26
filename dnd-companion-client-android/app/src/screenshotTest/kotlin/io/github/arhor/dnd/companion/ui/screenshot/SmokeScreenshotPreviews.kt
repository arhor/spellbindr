package io.github.arhor.dnd.companion.ui.screenshot

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import io.github.arhor.dnd.companion.ui.components.AppTopBar
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.AppTopBarNavigation
import io.github.arhor.dnd.companion.ui.components.ErrorMessage
import io.github.arhor.dnd.companion.ui.components.LoadingIndicator

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
