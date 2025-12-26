package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.Cost
import com.github.arhor.spellbindr.data.model.Damage
import com.github.arhor.spellbindr.data.model.Equipment
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeaponCatalogMapperTest {

    @Test
    fun `toWeaponCatalogEntryOrNull filters non-weapon equipment`() {
        val equipment = buildEquipment(
            id = "armor-1",
            categories = setOf(EquipmentCategory.ARMOR),
            damageDice = "1d8",
            damageTypeId = "slashing",
        )

        val result = equipment.toWeaponCatalogEntryOrNull()

        assertThat(result).isNull()
    }

    @Test
    fun `toWeaponCatalogEntryOrNull parses basic damage dice and damage type`() {
        val equipment = buildEquipment(
            id = "weapon-1",
            categories = setOf(EquipmentCategory.WEAPON),
            damageDice = "1d8",
            damageTypeId = "slashing",
        )

        val result = equipment.toWeaponCatalogEntryOrNull()

        requireNotNull(result)
        assertThat(result.damageDiceCount).isEqualTo(1)
        assertThat(result.damageDieSize).isEqualTo(8)
        assertThat(result.damageType).isEqualTo(DamageType.SLASHING)
    }

    @Test
    fun `toWeaponCatalogEntryOrNull ignores modifiers in damage dice`() {
        val equipment = buildEquipment(
            id = "weapon-2",
            categories = setOf(EquipmentCategory.WEAPON),
            damageDice = "2d6+1",
            damageTypeId = "piercing",
        )

        val result = equipment.toWeaponCatalogEntryOrNull()

        requireNotNull(result)
        assertThat(result.damageDiceCount).isEqualTo(2)
        assertThat(result.damageDieSize).isEqualTo(6)
    }
}

private fun buildEquipment(
    id: String,
    categories: Set<EquipmentCategory>,
    damageDice: String,
    damageTypeId: String,
): Equipment = Equipment(
    id = id,
    name = "Test",
    cost = Cost(quantity = 1, unit = "gp"),
    damage = Damage(
        damageDice = damageDice,
        damageType = EntityRef(damageTypeId),
    ),
    categories = categories,
)
