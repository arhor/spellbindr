package com.github.arhor.spellbindr.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.arhor.spellbindr.domain.model.EntityRef

/**
 * Room Entity representing a row in the "characters" table.
 *
 * This entity stores the core character data required for querying and listing,
 * as well as a full [manualSheet] snapshot for the detailed character editor.
 *
 * @property id Primary key.
 * @property name Character name (indexed for search/sorting, typically).
 * @property race Entity reference to the race.
 * @property subrace Optional entity reference to the subrace.
 * @property classes Map of class references to levels.
 * @property background Entity reference to the background.
 * @property abilityScores Map of ability references to scores.
 * @property proficiencies Set of proficiency references.
 * @property equipment Set of equipment references.
 * @property inventory Map of inventory items to counts.
 * @property spells Set of known spell references.
 * @property manualSheet Full serialized snapshot of the user's manual inputs (JSON).
 */
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: String,
    val name: String = "",
    val race: EntityRef = EntityRef("unknown"),
    val subrace: EntityRef? = null,
    val classes: Map<EntityRef, Int> = emptyMap(),
    val background: EntityRef = EntityRef("unknown"),
    val abilityScores: Map<EntityRef, Int> = emptyMap(),
    val proficiencies: Set<EntityRef> = emptySet(),
    val equipment: Set<EntityRef> = emptySet(),
    val inventory: Map<EntityRef, Int> = emptyMap(),
    val spells: Set<EntityRef> = emptySet(),
    val manualSheet: CharacterSheetSnapshot? = null,
)
