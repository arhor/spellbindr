package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Weapon
import java.util.UUID

@Immutable
data class WeaponEditorState(
    val id: String? = null,
    val catalogId: String? = null,
    val name: String = "",
    val category: EquipmentCategory? = null,
    val categories: Set<EquipmentCategory> = emptySet(),
    val abilityId: AbilityId = AbilityIds.STR,
    val proficient: Boolean = false,
    val useAbilityForDamage: Boolean = true,
    val damageDiceCount: String = "1",
    val damageDieSize: String = "6",
    val damageType: DamageType = DamageType.SLASHING,
) {
    fun toWeapon(): Weapon = Weapon(
        id = id ?: UUID.randomUUID().toString(),
        catalogId = catalogId,
        name = name.trim(),
        category = category,
        categories = categories,
        abilityId = abilityId,
        proficient = proficient,
        damageDiceCount = damageDiceCount.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        damageDieSize = damageDieSize.toIntOrNull()?.coerceAtLeast(1) ?: 6,
        useAbilityForDamage = useAbilityForDamage,
        damageType = damageType,
    )

    companion object {
        fun fromWeapon(weapon: Weapon): WeaponEditorState = WeaponEditorState(
            id = weapon.id,
            catalogId = weapon.catalogId,
            name = weapon.name,
            category = weapon.category,
            categories = weapon.categories,
            abilityId = weapon.abilityId,
            proficient = weapon.proficient,
            useAbilityForDamage = weapon.useAbilityForDamage,
            damageDiceCount = weapon.damageDiceCount.toString(),
            damageDieSize = weapon.damageDieSize.toString(),
            damageType = weapon.damageType,
        )
    }
}
