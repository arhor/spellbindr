package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Action(
    val name: String,
    val desc: String,
    @SerialName("attack_bonus")
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null,
    val options: Choice? = null,
    val usage: ActionUsage? = null,
    @SerialName("multiattack_type")
    val multiattackType: String,
    val actions: List<ActionItem>,
    @SerialName("action_options")
    val actionOptions: Choice
)
