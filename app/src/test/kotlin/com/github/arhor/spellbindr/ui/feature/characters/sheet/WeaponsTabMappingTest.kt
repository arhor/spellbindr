package com.github.arhor.spellbindr.ui.feature.characters.sheet

import com.github.arhor.spellbindr.data.model.AbilityScores
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.Weapon
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
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
                    attackAbility = Ability.STR,
                    proficient = true,
                    damageDiceCount = 1,
                    damageDieSize = 8,
                    damageAbility = Ability.STR,
                    damageType = DamageType.SLASHING,
                )
            ),
        )

        val weaponsState = sheet.toWeaponsState().weapons.single()

        assertThat(weaponsState.attackBonusLabel).isEqualTo("ATK +7")
        assertThat(weaponsState.damageLabel).isEqualTo("DMG 1d8 +4")
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
                    attackAbility = Ability.STR,
                    proficient = false,
                    damageDiceCount = 1,
                    damageDieSize = 6,
                    damageAbility = Ability.DEX,
                    damageType = DamageType.PIERCING,
                ),
                Weapon(
                    id = "weapon-3",
                    name = "Mind Spike",
                    attackAbility = Ability.INT,
                    proficient = true,
                    damageDiceCount = 2,
                    damageDieSize = 6,
                    damageAbility = Ability.INT,
                    damageType = DamageType.PSYCHIC,
                ),
            ),
        )

        val (shortsword, mindSpike) = sheet.toWeaponsState().weapons

        assertThat(shortsword.attackBonusLabel).isEqualTo("ATK +1")
        assertThat(shortsword.damageLabel).isEqualTo("DMG 1d6 -1")
        assertThat(mindSpike.attackBonusLabel).isEqualTo("ATK +5")
        assertThat(mindSpike.damageLabel).isEqualTo("DMG 2d6 +3")
    }
}
