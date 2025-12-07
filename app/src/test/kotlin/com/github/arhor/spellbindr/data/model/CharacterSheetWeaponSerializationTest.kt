package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterSheetWeaponSerializationTest {

    @Test
    fun `weapons round trip through snapshot`() {
        val weapon = Weapon(
            id = "weapon-1",
            name = "Longsword",
            attackBonus = 5,
            damage = "1d8+3",
            damageType = DamageType.SLASHING,
            ability = Ability.STR,
        )

        val sheet = CharacterSheet(id = "character-1", weapons = listOf(weapon))

        val snapshot = sheet.toSnapshot()
        val restored = snapshot.toDomain(sheet.id)

        assertThat(restored.weapons).containsExactly(weapon)
    }

    @Test
    fun `weapon defaults are preserved when attack bonus missing`() {
        val snapshot = CharacterSheetSnapshot(
            weapons = listOf(
                Weapon(
                    id = "weapon-2",
                    name = "Fire Bolt",
                    damageType = DamageType.FIRE,
                    ability = Ability.INT,
                )
            )
        )

        val restored = snapshot.toDomain("character-2")

        assertThat(restored.weapons).hasSize(1)
        assertThat(restored.weapons.first().attackBonus).isEqualTo(0)
        assertThat(restored.weapons.first().damage).isEqualTo("")
    }
}
