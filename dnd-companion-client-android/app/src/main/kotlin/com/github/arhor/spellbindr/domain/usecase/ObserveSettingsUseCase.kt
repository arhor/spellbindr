package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.settings
}
