package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
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
