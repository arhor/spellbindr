package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.domain.model.Cost
import com.github.arhor.spellbindr.domain.model.Damage
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeaponCatalogMapperTest {

    @Test
    fun `toWeaponCatalogEntryOrNull should return null when equipment is not a weapon`() {
        // Given
        val equipment = buildEquipment(
            id = "armor-1",
            categories = setOf(EquipmentCategory.ARMOR),
            damageDice = "1d8",
            damageTypeId = "slashing",
        )

        // When
        val result = equipment.toWeaponCatalogEntryOrNull()

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `toWeaponCatalogEntryOrNull should parse damage dice and type when weapon is valid`() {
        // Given
        val equipment = buildEquipment(
            id = "weapon-1",
            categories = setOf(EquipmentCategory.WEAPON),
            damageDice = "1d8",
            damageTypeId = "slashing",
        )

        // When
        val result = equipment.toWeaponCatalogEntryOrNull()

        // Then
        requireNotNull(result)
        assertThat(result.damageDiceCount).isEqualTo(1)
        assertThat(result.damageDieSize).isEqualTo(8)
        assertThat(result.damageType).isEqualTo(DamageType.SLASHING)
    }

    @Test
    fun `toWeaponCatalogEntryOrNull should ignore modifiers when parsing damage dice`() {
        // Given
        val equipment = buildEquipment(
            id = "weapon-2",
            categories = setOf(EquipmentCategory.WEAPON),
            damageDice = "2d6+1",
            damageTypeId = "piercing",
        )

        // When
        val result = equipment.toWeaponCatalogEntryOrNull()

        // Then
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
