package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Language
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.LanguagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllLanguagesUseCase @Inject constructor(
    private val languagesRepository: LanguagesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Language>>> =
        languagesRepository.allLanguagesState
}

