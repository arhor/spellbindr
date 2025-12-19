@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.theme.AppTheme

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
fun AppTopBar(config: AppTopBarConfig) {
    if (!config.visible) return

    TopAppBar(
        title = config.title,
        navigationIcon = config.navigation.asNavigationIcon(),
        actions = config.actions,
    )
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
        AppTopBar(
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
        )
    }
}
