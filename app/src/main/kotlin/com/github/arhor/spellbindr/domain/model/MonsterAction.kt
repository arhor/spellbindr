package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an action a monster can take.
 *
 * @property name The name of the action.
 * @property desc A description of the action.
 * @property attackBonus The attack bonus for the action, if applicable.
 * @property damage A list of damage types and amounts for the action, if applicable.
 * @property dc The difficulty class for any saving throws associated with the action, if applicable.
 * @property options A choice of options for the action, if applicable.
 * @property usage How the action can be used (e.g., recharge, per day).
 * @property multiAttackType The type of multiattack, if this action is a multiattack.
 * @property actions A list of sub-actions if this action is a multiattack or has multiple parts.
 * @property actionOptions A choice of actions if the creature can choose between multiple actions.
 */
@Serializable
data class MonsterAction(
    val name: String,
    val desc: String,
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null,
    val options: Choice? = null,
    val usage: ActionUsage? = null,
    val multiAttackType: String,
    val actions: List<ActionItem>,
    val actionOptions: Choice,
)
