package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponCatalogUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponEditorState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.WeaponsTabState
import com.github.arhor.spellbindr.utils.signed

internal fun WeaponCatalogEntry.toUiModel(): WeaponCatalogUiModel {
    val category = categories.firstOrNull { it != EquipmentCategory.WEAPON }
        ?: categories.firstOrNull()
    return WeaponCatalogUiModel(
        id = id,
        name = name,
        category = category,
        categories = categories,
        damageDiceCount = damageDiceNum,
        damageDieSize = damageDieSize,
        damageType = damageType,
    )
}

internal fun WeaponCatalogUiModel.toEditorState(): WeaponEditorState = WeaponEditorState(
    catalogId = id,
    name = name,
    category = category,
    categories = categories,
    damageDiceCount = damageDiceCount.toString(),
    damageDieSize = damageDieSize.toString(),
    damageType = damageType,
)

internal fun CharacterSheet.toWeaponsState(): WeaponsTabState {
    val scores: AbilityScores = abilityScores
    val proficiency = proficiencyBonus

    return WeaponsTabState(
        weapons = weapons.map { weapon ->
            val abilityModifier = scores.modifierFor(weapon.abilityId)
            val attackBonus = abilityModifier + if (weapon.proficient) proficiency else 0
            val damageBonus = if (weapon.useAbilityForDamage) abilityModifier else 0
            val damagePart = if (damageBonus == 0) {
                "${weapon.damageDiceCount}d${weapon.damageDieSize}"
            } else {
                "${weapon.damageDiceCount}d${weapon.damageDieSize}${signed(damageBonus)}"
            }

            WeaponUiModel(
                id = weapon.id,
                name = weapon.name.ifBlank { "Unnamed weapon" },
                attackBonusLabel = "ATK ${signed(attackBonus)}",
                damageLabel = "DMG $damagePart",
                damageType = weapon.damageType,
            )
        },
    )
}
