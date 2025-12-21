package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Weapon
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class UpdateWeaponListUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = UpdateWeaponListUseCase()

    @Test
    fun `save weapon upserts by id`() {
        val weapon = Weapon(id = "w1", name = "Sword")
        val initial = CharacterSheet(id = "hero")

        val added = useCase(initial, UpdateWeaponListUseCase.Action.Save(weapon))
        val updated = useCase(added, UpdateWeaponListUseCase.Action.Save(weapon.copy(name = "Longsword")))

        assertThat(added.weapons).hasSize(1)
        assertThat(updated.weapons).hasSize(1)
        assertThat(updated.weapons.first().name).isEqualTo("Longsword")
    }

    @Test
    fun `delete weapon removes matching id`() {
        val weapon = Weapon(id = "w1", name = "Sword")
        val other = Weapon(id = "w2", name = "Axe")
        val initial = CharacterSheet(id = "hero", weapons = listOf(weapon, other))

        val updated = useCase(initial, UpdateWeaponListUseCase.Action.Delete("w1"))

        assertThat(updated.weapons).containsExactly(other)
    }
}
