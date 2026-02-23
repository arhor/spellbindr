package com.github.arhor.spellbindr.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
data class TopBarState(
    val config: AppTopBarConfig? = null,
    val overlays: @Composable () -> Unit = {},
) {
    companion object {
        val Empty = TopBarState()
    }
}

val LocalTopBarState = staticCompositionLocalOf<MutableState<TopBarState>> {
    error("TopBarState provider is missing")
}

@Composable
fun ProvideTopBarState(
    topBarState: TopBarState,
    content: @Composable () -> Unit,
) {
    val holder = LocalTopBarState.current
    val latestTopBarState = rememberUpdatedState(topBarState)

    SideEffect {
        holder.value = topBarState
    }

    DisposableEffect(Unit) {
        onDispose {
            if (holder.value == latestTopBarState.value) {
                holder.value = latestTopBarState.value.copy(overlays = {})
            }
        }
    }

    content()
}

@Composable
fun rememberTopBarStateHolder(): MutableState<TopBarState> = remember { mutableStateOf(TopBarState.Empty) }
