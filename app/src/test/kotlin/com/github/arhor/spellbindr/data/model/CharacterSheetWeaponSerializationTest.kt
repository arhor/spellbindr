package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.model.EquipmentCategory
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class CharacterSheetWeaponSerializationTest {

    @Test
    fun `weapons round trip through snapshot`() {
        val weapon = Weapon(
            id = "weapon-1",
            catalogId = "catalog-longsword",
            name = "Longsword",
            category = EquipmentCategory.MARTIAL,
            categories = setOf(EquipmentCategory.MARTIAL, EquipmentCategory.MELEE),
            ability = Ability.STR,
            proficient = true,
            damageDiceCount = 1,
            damageDieSize = 8,
            damageType = DamageType.SLASHING,
        )

        val sheet = CharacterSheet(id = "character-1", weapons = listOf(weapon))

        val snapshot = sheet.toSnapshot()
        val restored = snapshot.toDomain(sheet.id)

        assertThat(restored.weapons).containsExactly(weapon)
    }

    @Test
    fun `weapon defaults are preserved when optional fields missing`() {
        val snapshot = CharacterSheetSnapshot(
            weapons = listOf(
                Weapon(
                    id = "weapon-2",
                    name = "Fire Bolt",
                    ability = Ability.INT,
                    useAbilityForDamage = false,
                    damageType = DamageType.FIRE,
                )
            )
        )

        val restored = snapshot.toDomain("character-2")

        assertThat(restored.weapons).hasSize(1)
        assertThat(restored.weapons.first().proficient).isFalse()
        assertThat(restored.weapons.first().damageDiceCount).isEqualTo(1)
        assertThat(restored.weapons.first().damageDieSize).isEqualTo(6)
        assertThat(restored.weapons.first().useAbilityForDamage).isFalse()
    }

    @Test
    fun `weapon defaults apply when legacy snapshot omits new fields`() {
        val json = Json { ignoreUnknownKeys = true }
        val snapshot = json.decodeFromString(
            CharacterSheetSnapshot.serializer(),
            """
            {
              "weapons": [
                {
                  "id": "weapon-legacy",
                  "name": "Dagger",
                  "attackAbility": "dex",
                  "proficient": true,
                  "damageDiceCount": 1,
                  "damageDieSize": 4,
                  "useAbilityForDamage": true,
                  "damageType": "piercing"
                }
              ]
            }
            """.trimIndent()
        )

        val weapon = snapshot.weapons.first()

        assertThat(weapon.catalogId).isNull()
        assertThat(weapon.category).isNull()
        assertThat(weapon.categories).isEmpty()
    }
}
