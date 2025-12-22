package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.model.Cost
import com.github.arhor.spellbindr.data.model.Damage
import com.github.arhor.spellbindr.data.model.Equipment
import com.github.arhor.spellbindr.data.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WeaponCatalogRepositoryImplTest {

    @Test
    fun `weaponCatalogEntries filters and maps weapons`() = runTest {
        val equipmentRepository = mockk<EquipmentRepository>()
        every { equipmentRepository.allEquipment } returns flowOf(
            listOf(
                buildEquipment(
                    id = "weapon-1",
                    categories = setOf(EquipmentCategory.WEAPON),
                    damageDice = "1d6",
                    damageTypeId = "slashing",
                ),
                buildEquipment(
                    id = "armor-1",
                    categories = setOf(EquipmentCategory.ARMOR),
                    damageDice = "1d8",
                    damageTypeId = "piercing",
                ),
            )
        )

        val repository = WeaponCatalogRepositoryImpl(equipmentRepository)

        val result = repository.weaponCatalogEntries.first()

        assertThat(result).hasSize(1)
        assertThat(result.first().id).isEqualTo("weapon-1")
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
