package io.github.arhor.dnd.companion.ui.feature.character.levelup

sealed interface CharacterLevelUpEffect {
    data object Cancelled : CharacterLevelUpEffect
    data object Completed : CharacterLevelUpEffect
    data class Message(val text: String) : CharacterLevelUpEffect
}
