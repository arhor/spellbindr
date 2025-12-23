package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.DamageType
import com.github.arhor.spellbindr.data.model.Equipment
import com.github.arhor.spellbindr.data.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry

private const val defaultDiceCount = 1
private const val defaultDieSize = 6
private val damageDiceRegex = Regex("""(\d+)\s*[dD]\s*(\d+)""")

fun Equipment.toWeaponCatalogEntryOrNull(): WeaponCatalogEntry? {
    if (!categories.contains(EquipmentCategory.WEAPON) || damage == null) {
        return null
    }

    val (diceCount, dieSize) = parseDamageDice(damage.damageDice)

    return WeaponCatalogEntry(
        id = id,
        name = name,
        categories = categories,
        damageDiceCount = diceCount,
        damageDieSize = dieSize,
        damageType = mapDamageType(damage.damageType.id),
    )
}

private fun parseDamageDice(damageDice: String): Pair<Int, Int> {
    val match = damageDiceRegex.find(damageDice) ?: return defaultDiceCount to defaultDieSize
    val diceCount = match.groupValues[1].toIntOrNull() ?: defaultDiceCount
    val dieSize = match.groupValues[2].toIntOrNull() ?: defaultDieSize
    return diceCount to dieSize
}

private fun mapDamageType(id: String): DamageType =
    DamageType.entries.firstOrNull { type -> type.name.equals(id, ignoreCase = true) } ?: DamageType.SLASHING
