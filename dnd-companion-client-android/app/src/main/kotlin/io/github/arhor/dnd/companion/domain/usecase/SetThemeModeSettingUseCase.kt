package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.ThemeMode
import io.github.arhor.dnd.companion.domain.repository.SettingsRepository
import javax.inject.Inject

class SetThemeModeSettingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode?) {
        settingsRepository.setThemeMode(mode)
    }
}
