package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository,
) {
    suspend operator fun invoke(mode: ThemeMode?) {
        themeRepository.setThemeMode(mode)
    }
}
