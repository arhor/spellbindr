package io.github.arhor.dnd.companion.data.local.database.entity

import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.CharacterSpell
import io.github.arhor.dnd.companion.domain.model.DeathSaveState
import io.github.arhor.dnd.companion.domain.model.PactSlotState
import io.github.arhor.dnd.companion.domain.model.SavingThrowEntry
import io.github.arhor.dnd.companion.domain.model.SkillEntry
import io.github.arhor.dnd.companion.domain.model.SpellSlotState
import io.github.arhor.dnd.companion.domain.model.Weapon
import io.github.arhor.dnd.companion.domain.model.ManagedProgressionSheetState
import io.github.arhor.dnd.companion.domain.model.defaultSavingThrows
import io.github.arhor.dnd.companion.domain.model.defaultSkills
import io.github.arhor.dnd.companion.domain.model.defaultSpellSlots
import kotlinx.serialization.Serializable

/**
 * Snapshot stored in Room (id is stored on the [CharacterEntity]).
 */
@Serializable
data class CharacterSheetSnapshot(
    val name: String = "",
    val level: Int = 1,
    val className: String = "",
    val race: String = "",
    val background: String = "",
    val alignment: String = "",
    val experiencePoints: Int? = null,
    val abilityScores: AbilityScores = AbilityScores(),
    val proficiencyBonus: Int = 2,
    val inspiration: Boolean = false,
    val maxHitPoints: Int = 1,
    val currentHitPoints: Int = 1,
    val temporaryHitPoints: Int = 0,
    val armorClass: Int = 10,
    val initiative: Int = 0,
    val speed: String = "",
    val hitDice: String = "",
    val deathSaves: DeathSaveState = DeathSaveState(),
    val spellSlots: List<SpellSlotState> = defaultSpellSlots(),
    val pactSlots: PactSlotState? = null,
    val concentrationSpellId: String? = null,
    val savingThrows: List<SavingThrowEntry> = defaultSavingThrows(),
    val skills: List<SkillEntry> = defaultSkills(),
    val senses: String = "",
    val languages: String = "",
    val proficiencies: String = "",
    val attacksAndCantrips: String = "",
    val featuresAndTraits: String = "",
    val equipment: String = "",
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = "",
    val notes: String = "",
    val characterSpells: List<CharacterSpell> = emptyList(),
    val weapons: List<Weapon> = emptyList(),
    /** Added with a default so snapshots written before level-up support remain decodable. */
    val manualProficiencyIds: Set<String> = emptySet(),
    val managedProgression: ManagedProgressionSheetState? = null,
)
