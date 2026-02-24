package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeThemeRepository(
    initialMode: ThemeMode? = null,
) : ThemeRepository {
    private val modeState = MutableStateFlow(initialMode)

    override val themeMode: Flow<ThemeMode?> = modeState.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode?) {
        modeState.value = mode
    }
}
