@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Simple configuration object describing how the shared app bar should look.
 */
@Stable
data class AppTopBarConfig(
    val visible: Boolean = false,
    val title: @Composable () -> Unit = {},
    val navigation: AppTopBarNavigation = AppTopBarNavigation.None,
    val actions: @Composable RowScope.() -> Unit = {},
) {
    companion object {
        val None = AppTopBarConfig()
    }
}

@Stable
sealed interface AppTopBarNavigation {
    data object None : AppTopBarNavigation
    data class Back(val onClick: () -> Unit) : AppTopBarNavigation
    data class Custom(val content: @Composable () -> Unit) : AppTopBarNavigation
}

interface AppTopBarController {
    val config: State<AppTopBarConfig>
    fun setTopBar(owner: Any, config: AppTopBarConfig)
    fun clearTopBar(owner: Any)
}

@Composable
fun rememberAppTopBarController(): AppTopBarController {
    val configState = remember { mutableStateOf(AppTopBarConfig.None) }
    return remember { AppTopBarControllerImpl(configState) }
}

val LocalAppTopBarController = staticCompositionLocalOf<AppTopBarController> {
    NoOpAppTopBarController
}

@Composable
fun AppTopBar(config: AppTopBarConfig) {
    if (!config.visible) return

    TopAppBar(
        title = config.title,
        navigationIcon = config.navigation.asNavigationIcon(),
        actions = config.actions,
    )
}

private fun AppTopBarNavigation.asNavigationIcon(): @Composable () -> Unit = when (this) {
    AppTopBarNavigation.None -> EmptyNavigationIcon
    is AppTopBarNavigation.Back -> {
        val backClick = onClick
        {
            IconButton(onClick = backClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    }

    is AppTopBarNavigation.Custom -> content
}

private val EmptyNavigationIcon: @Composable () -> Unit = {}

private class AppTopBarControllerImpl(
    private val state: MutableState<AppTopBarConfig>,
) : AppTopBarController {

    private var ownerRef: Any? = null

    override val config: State<AppTopBarConfig> = state

    override fun setTopBar(owner: Any, config: AppTopBarConfig) {
        ownerRef = owner
        state.value = config
    }

    override fun clearTopBar(owner: Any) {
        if (ownerRef === owner) {
            ownerRef = null
            state.value = AppTopBarConfig.None
        }
    }
}

@Composable
fun ProvideTopBar(config: AppTopBarConfig) {
    val controller = LocalAppTopBarController.current
    val owner = remember { Any() }

    SideEffect {
        controller.setTopBar(owner, config)
    }

    DisposableEffect(owner) {
        onDispose { controller.clearTopBar(owner) }
    }
}

private object NoOpAppTopBarController : AppTopBarController {
    private val internalState = mutableStateOf(AppTopBarConfig.None)

    override val config: State<AppTopBarConfig> = internalState

    override fun setTopBar(owner: Any, config: AppTopBarConfig) {
        internalState.value = config
    }

    override fun clearTopBar(owner: Any) {
        internalState.value = AppTopBarConfig.None
    }
}
