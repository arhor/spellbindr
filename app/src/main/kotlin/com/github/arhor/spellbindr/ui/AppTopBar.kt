@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

private val LocalAppTopBarController = staticCompositionLocalOf<AppTopBarController?> { null }
private val EmptyNavigationIcon: @Composable (() -> Unit) = {}

/**
 * Simple configuration object describing how the shared app bar should look.
 */
@Stable
data class AppTopBarConfig(
    val visible: Boolean = false,
    val title: @Composable (() -> Unit) = {},
    val navigation: AppTopBarNavigation = AppTopBarNavigation.None,
    val actions: @Composable (RowScope.() -> Unit) = {},
) {
    companion object {
        val None = AppTopBarConfig()
    }
}

class AppTopBarController(
    private val state: MutableState<AppTopBarConfig>,
) {
    private var ownerRef: Any? = null

    fun setTopBar(owner: Any, config: AppTopBarConfig) {
        ownerRef = owner
        state.value = config
    }

    fun clearTopBar(owner: Any) {
        if (ownerRef === owner) {
            ownerRef = null
            state.value = AppTopBarConfig.None
        }
    }
}

@Stable
sealed interface AppTopBarNavigation {

    fun asNavigationIcon(): @Composable (() -> Unit)

    data object None : AppTopBarNavigation {
        override fun asNavigationIcon(): @Composable (() -> Unit) = EmptyNavigationIcon
    }

    data class Back(val onClick: () -> Unit) : AppTopBarNavigation {
        override fun asNavigationIcon(): @Composable (() -> Unit) = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    }

    data class Custom(val content: @Composable () -> Unit) : AppTopBarNavigation {
        override fun asNavigationIcon(): @Composable (() -> Unit) = content
    }
}

@Composable
fun AppTopBarControllerProvider(content: @Composable (AppTopBarConfig) -> Unit) {
    val currConfig = remember { mutableStateOf(AppTopBarConfig.None) }
    val controller = remember { AppTopBarController(currConfig) }

    CompositionLocalProvider(LocalAppTopBarController provides controller) {
        content(currConfig.value)
    }
}

@Composable
fun WithAppTopBar(
    config: AppTopBarConfig,
    content: @Composable () -> Unit,
) {
    val controller = LocalAppTopBarController.current
    val currentRef = remember { Any() }

    if (controller != null) {
        DisposableEffect(currentRef, config) {
            controller.setTopBar(currentRef, config)
            onDispose { controller.clearTopBar(currentRef) }
        }
    }
    content()
}

@Preview
@Composable
private fun AppTopBarLightPreview() {
    AppTopBarPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun AppTopBarDarkPreview() {
    AppTopBarPreview(isDarkTheme = true)
}

@Composable
private fun AppTopBarPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        AppTopBarControllerProvider { config ->
            Scaffold(
                topBar = {
                    if (config.visible) {
                        TopAppBar(
                            title = config.title,
                            navigationIcon = config.navigation.asNavigationIcon(),
                            actions = config.actions,
                        )
                    }
                },
            ) { innerPadding ->
                WithAppTopBar(
                    config = AppTopBarConfig(
                        visible = true,
                        title = { Text(text = "Spellbindr") },
                        navigation = AppTopBarNavigation.Back {},
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                    ),
                ) {
                    Text(
                        text = "Preview content",
                        modifier = Modifier.padding(innerPadding).padding(16.dp),
                    )
                }
            }
        }
    }
}
