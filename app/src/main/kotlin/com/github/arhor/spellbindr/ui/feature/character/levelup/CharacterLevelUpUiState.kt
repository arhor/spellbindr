package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.serialization.Serializable

@Serializable
enum class CharacterLevelUpStep(val title: String) {
    Class("Class"),
    Choices("Subclass & choices"),
    HitPoints("Hit points"),
    AbilityScore("ASI or feat"),
    Spells("Spells"),
    Review("Review"),
}

internal fun characterLevelUpSteps(requirements: List<LevelUpRequirement>): List<CharacterLevelUpStep> = buildList {
    add(CharacterLevelUpStep.Class)
    if (requirements.any { it is LevelUpRequirement.SubclassSelection || it is LevelUpRequirement.ChoiceSelection }) {
        add(CharacterLevelUpStep.Choices)
    }
    if (requirements.any { it is LevelUpRequirement.HitPoints }) add(CharacterLevelUpStep.HitPoints)
    if (requirements.any { it is LevelUpRequirement.AbilityScoreImprovement }) add(CharacterLevelUpStep.AbilityScore)
    if (requirements.any { it is LevelUpRequirement.SpellDecisions }) add(CharacterLevelUpStep.Spells)
    add(CharacterLevelUpStep.Review)
}

sealed interface CharacterLevelUpUiState {
    @Immutable data object Loading : CharacterLevelUpUiState
    @Immutable data class Failure(val message: String) : CharacterLevelUpUiState
    @Immutable data class Unavailable(val title: String, val explanation: String) : CharacterLevelUpUiState

    @Immutable
    data class Content(
        val characterName: String,
        val plan: LevelUpPlan,
        val preview: LevelUpPreview,
        val classes: List<CharacterClass>,
        val feats: List<Feat>,
        val spells: List<Spell>,
        val steps: List<CharacterLevelUpStep>,
        val step: CharacterLevelUpStep,
        val currentStepIndex: Int,
        val isSaving: Boolean = false,
        val staleMessage: String? = null,
        val persistenceMessage: String? = null,
    ) : CharacterLevelUpUiState {
        val isReview: Boolean get() = step == CharacterLevelUpStep.Review
        val canConfirm: Boolean get() = isReview && preview.canConfirm && !isSaving
    }
}
