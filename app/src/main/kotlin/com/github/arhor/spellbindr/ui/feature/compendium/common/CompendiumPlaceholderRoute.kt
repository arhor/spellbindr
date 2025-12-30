package com.github.arhor.spellbindr.ui.feature.compendium.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun CompendiumPlaceholderRoute(
    title: String,
    onBack: () -> Unit,
) {
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = title) },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "$title coming soon")
        }
    }
}
