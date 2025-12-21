package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository,
) {
    operator fun invoke(): Flow<ThemeMode?> = themeRepository.themeMode
}
