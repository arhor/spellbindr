package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.ApplyLevelUpResult
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpReferenceData
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import javax.inject.Inject

/** Confirms a reviewed plan through the repository's single atomic write boundary. */
class ApplyLevelUpUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(
        characterId: String,
        expectedTotalLevel: Int,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): ApplyLevelUpResult = characterRepository.applyLevelUp(
        characterId = characterId,
        expectedTotalLevel = expectedTotalLevel,
        plan = plan,
        referenceData = referenceData,
    )
}
