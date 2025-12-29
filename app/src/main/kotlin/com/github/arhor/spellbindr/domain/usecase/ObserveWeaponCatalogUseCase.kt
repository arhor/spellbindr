package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.data.repository.EquipmentRepository
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import com.github.arhor.spellbindr.utils.unwrap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveWeaponCatalogUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
) {
    operator fun invoke(): Flow<List<WeaponCatalogEntry>> =
        equipmentRepository
            .allEquipmentState
            .unwrap()
            .map(::toWeaponCatalogEntries)

    private fun toWeaponCatalogEntries(equipment: List<Equipment>): List<WeaponCatalogEntry> =
        equipment.mapNotNull {
            if (EquipmentCategory.WEAPON in it.categories && it.damage != null) {
                val (diceNum, dieSize) = parseDamageDice(it.damage.damageDice)

                WeaponCatalogEntry(
                    id = it.id,
                    name = it.name,
                    categories = it.categories,
                    damageDiceNum = diceNum,
                    damageDieSize = dieSize,
                    damageType = mapDamageType(it.damage.damageType.id),
                )
            } else {
                null
            }
        }

    private fun parseDamageDice(damageDice: String): Pair<Int, Int> {
        val match = damageDiceRegex.find(damageDice)
            ?: return DEFAULT_DICE_NUM to DEFAULT_DIE_SIZE

        val diceNum = match.groupValues[1].toIntOrNull() ?: DEFAULT_DICE_NUM
        val dieSize = match.groupValues[2].toIntOrNull() ?: DEFAULT_DIE_SIZE

        return diceNum to dieSize
    }

    private fun mapDamageType(id: String): DamageType =
        DamageType.entries.firstOrNull { type -> type.name.equals(id, ignoreCase = true) }
            ?: DamageType.SLASHING

    companion object {
        private const val DEFAULT_DICE_NUM = 1
        private const val DEFAULT_DIE_SIZE = 6

        private val damageDiceRegex = Regex("""(\d+)\s*[dD]\s*(\d+)""")
    }
}
