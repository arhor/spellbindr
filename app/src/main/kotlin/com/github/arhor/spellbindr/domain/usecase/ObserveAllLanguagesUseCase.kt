package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.LanguagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllLanguagesUseCase @Inject constructor(
    private val languagesRepository: LanguagesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Language>>> =
        languagesRepository.allLanguagesState
}

