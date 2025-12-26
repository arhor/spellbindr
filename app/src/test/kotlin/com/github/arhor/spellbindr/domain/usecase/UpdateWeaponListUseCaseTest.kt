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
    fun `invoke should upsert weapon by id when saving`() {
        // Given
        val weapon = Weapon(id = "w1", name = "Sword")
        val initial = CharacterSheet(id = "hero")

        // When
        val added = useCase(initial, UpdateWeaponListUseCase.Action.Save(weapon))
        val updated = useCase(added, UpdateWeaponListUseCase.Action.Save(weapon.copy(name = "Longsword")))

        // Then
        assertThat(added.weapons).hasSize(1)
        assertThat(updated.weapons).hasSize(1)
        assertThat(updated.weapons.first().name).isEqualTo("Longsword")
    }

    @Test
    fun `invoke should remove matching weapon when delete action is applied`() {
        // Given
        val weapon = Weapon(id = "w1", name = "Sword")
        val other = Weapon(id = "w2", name = "Axe")
        val initial = CharacterSheet(id = "hero", weapons = listOf(weapon, other))

        // When
        val updated = useCase(initial, UpdateWeaponListUseCase.Action.Delete("w1"))

        // Then
        assertThat(updated.weapons).containsExactly(other)
    }
}
