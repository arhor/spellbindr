package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.SettingsRepository
import javax.inject.Inject

class SetThemeModeSettingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode?) {
        settingsRepository.setThemeMode(mode)
    }
}
