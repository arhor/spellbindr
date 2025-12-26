package com.github.arhor.spellbindr.ui.feature.characters.sheet

import com.github.arhor.spellbindr.data.model.DamageType
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Weapon
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeaponsTabMappingTest {

    @Test
    fun `attack bonus uses ability modifier plus proficiency when proficient`() {
        val sheet = CharacterSheet(
            id = "character-1",
            abilityScores = AbilityScores(strength = 18),
            proficiencyBonus = 3,
            weapons = listOf(
                Weapon(
                    id = "weapon-1",
                    name = "Longsword",
                    abilityId = AbilityIds.STR,
                    proficient = true,
                    damageDiceCount = 1,
                    damageDieSize = 8,
                    damageType = DamageType.SLASHING,
                )
            ),
        )

        val weaponsState = sheet.toWeaponsState().weapons.single()

        assertThat(weaponsState.attackBonusLabel).isEqualTo("ATK +7")
        assertThat(weaponsState.damageLabel).isEqualTo("DMG 1d8+4")
    }

    @Test
    fun `damage bonus follows damage ability and handles negative modifiers`() {
        val sheet = CharacterSheet(
            id = "character-2",
            abilityScores = AbilityScores(strength = 12, dexterity = 8, intelligence = 16),
            proficiencyBonus = 2,
            weapons = listOf(
                Weapon(
                    id = "weapon-2",
                    name = "Shortsword",
                    abilityId = AbilityIds.DEX,
                    proficient = true,
                    damageDiceCount = 1,
                    damageDieSize = 6,
                    useAbilityForDamage = true,
                    damageType = DamageType.PIERCING,
                ),
                Weapon(
                    id = "weapon-3",
                    name = "Mind Spike",
                    abilityId = AbilityIds.INT,
                    proficient = true,
                    damageDiceCount = 2,
                    damageDieSize = 6,
                    damageType = DamageType.PSYCHIC,
                ),
            ),
        )

        val (shortsword, mindSpike) = sheet.toWeaponsState().weapons

        assertThat(shortsword.attackBonusLabel).isEqualTo("ATK +1")
        assertThat(shortsword.damageLabel).isEqualTo("DMG 1d6-1")
        assertThat(mindSpike.attackBonusLabel).isEqualTo("ATK +5")
        assertThat(mindSpike.damageLabel).isEqualTo("DMG 2d6+3")
    }

    @Test
    fun `damage omits ability bonus when toggle disabled`() {
        val sheet = CharacterSheet(
            id = "character-3",
            abilityScores = AbilityScores(strength = 16, dexterity = 14),
            proficiencyBonus = 2,
            weapons = listOf(
                Weapon(
                    id = "weapon-4",
                    name = "Unarmed Strike",
                    abilityId = AbilityIds.STR,
                    proficient = true,
                    damageDiceCount = 1,
                    damageDieSize = 4,
                    useAbilityForDamage = false,
                    damageType = DamageType.BLUDGEONING,
                ),
            ),
        )

        val weapon = sheet.toWeaponsState().weapons.single()

        assertThat(weapon.attackBonusLabel).isEqualTo("ATK +5")
        assertThat(weapon.damageLabel).isEqualTo("DMG 1d4")
    }
}
