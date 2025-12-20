package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(
    private val themeRepository: ThemeRepository,
) {
    operator fun invoke(): Flow<ThemeMode?> = themeRepository.themeMode
}
