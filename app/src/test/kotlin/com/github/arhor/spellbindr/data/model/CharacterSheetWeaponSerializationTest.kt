package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.local.db.CharacterSheetSnapshot
import com.github.arhor.spellbindr.data.local.db.toDomain
import com.github.arhor.spellbindr.data.local.db.toSnapshot
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Weapon
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class CharacterSheetWeaponSerializationTest {

    @Test
    fun `toSnapshot should round trip weapons when converting to domain and back`() {
        // Given
        val weapon = Weapon(
            id = "weapon-1",
            catalogId = "catalog-longsword",
            name = "Longsword",
            category = EquipmentCategory.MARTIAL,
            categories = setOf(EquipmentCategory.MARTIAL, EquipmentCategory.MELEE),
            abilityId = AbilityIds.STR,
            proficient = true,
            damageDiceCount = 1,
            damageDieSize = 8,
            damageType = DamageType.SLASHING,
        )

        val sheet = CharacterSheet(id = "character-1", weapons = listOf(weapon))

        // When
        val snapshot = sheet.toSnapshot()
        val restored = snapshot.toDomain(sheet.id)

        // Then
        assertThat(restored.weapons).containsExactly(weapon)
    }

    @Test
    fun `toDomain should preserve weapon defaults when optional fields are missing`() {
        // Given
        val snapshot = CharacterSheetSnapshot(
            weapons = listOf(
                Weapon(
                    id = "weapon-2",
                    name = "Fire Bolt",
                    abilityId = AbilityIds.INT,
                    useAbilityForDamage = false,
                    damageType = DamageType.FIRE,
                )
            )
        )

        // When
        val restored = snapshot.toDomain("character-2")

        // Then
        assertThat(restored.weapons).hasSize(1)
        assertThat(restored.weapons.first().proficient).isFalse()
        assertThat(restored.weapons.first().damageDiceCount).isEqualTo(1)
        assertThat(restored.weapons.first().damageDieSize).isEqualTo(6)
        assertThat(restored.weapons.first().useAbilityForDamage).isFalse()
    }

    @Test
    fun `toDomain should apply weapon defaults when legacy snapshot omits new fields`() {
        // Given
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

        // When
        val weapon = snapshot.weapons.first()

        // Then
        assertThat(weapon.catalogId).isNull()
        assertThat(weapon.category).isNull()
        assertThat(weapon.categories).isEmpty()
    }
}
