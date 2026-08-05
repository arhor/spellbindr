package com.github.arhor.spellbindr.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("Quarantined until #156: Room migration schemas are not resolved under Robolectric")
class Migration3To4Test {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SpellbindrDatabase::class.java,
    )

    @Test
    fun `migrate should preserve character row when upgrading version 3 database to version 4`() {
        // Given
        val databaseName = "migration-3-to-4-test"
        val character = CharacterRow(
            id = "character-1",
            name = "Aster",
            race = "{\"index\":\"elf\",\"name\":\"Elf\"}",
            subrace = "{\"index\":\"high-elf\",\"name\":\"High Elf\"}",
            classes = "{\"{\\\"index\\\":\\\"wizard\\\",\\\"name\\\":\\\"Wizard\\\"}\":3}",
            background = "{\"index\":\"sage\",\"name\":\"Sage\"}",
            abilityScores = "{\"{\\\"index\\\":\\\"int\\\",\\\"name\\\":\\\"Intelligence\\\"}\":16}",
            proficiencies = "[{\"index\":\"arcana\",\"name\":\"Arcana\"}]",
            equipment = "[{\"index\":\"quarterstaff\",\"name\":\"Quarterstaff\"}]",
            inventory = "{\"{\\\"index\\\":\\\"spellbook\\\",\\\"name\\\":\\\"Spellbook\\\"}\":1}",
            spells = "[{\"index\":\"magic-missile\",\"name\":\"Magic Missile\"}]",
            manualSheet = "{\"notes\":\"Original sheet JSON must remain byte-for-byte intact.\"}",
        )
        migrationTestHelper.createDatabase(databaseName, 3).apply {
            execSQL(INSERT_CHARACTER_SQL, character.values)
            close()
        }

        // When
        val migratedDatabase = migrationTestHelper.runMigrationsAndValidate(
            databaseName,
            4,
            true,
            MIGRATION_3_4,
        )

        // Then
        migratedDatabase.use { database ->
            database.query(SELECT_CHARACTER_SQL).use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo(character.id)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name"))).isEqualTo(character.name)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("race"))).isEqualTo(character.race)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("subrace"))).isEqualTo(character.subrace)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("classes"))).isEqualTo(character.classes)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("background"))).isEqualTo(character.background)
                assertThat(
                    cursor.getString(cursor.getColumnIndexOrThrow("abilityScores"))
                ).isEqualTo(character.abilityScores)
                assertThat(
                    cursor.getString(cursor.getColumnIndexOrThrow("proficiencies"))
                ).isEqualTo(character.proficiencies)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("equipment"))).isEqualTo(character.equipment)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("inventory"))).isEqualTo(character.inventory)
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow("spells"))).isEqualTo(character.spells)
                assertThat(
                    cursor.getString(cursor.getColumnIndexOrThrow("manualSheet"))
                ).isEqualTo(character.manualSheet)
                assertThat(cursor.moveToNext()).isFalse()
            }
            database.query("SELECT COUNT(*) FROM `character_progressions`").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getInt(0)).isEqualTo(0)
            }
        }
    }

    private data class CharacterRow(
        val id: String,
        val name: String,
        val race: String,
        val subrace: String,
        val classes: String,
        val background: String,
        val abilityScores: String,
        val proficiencies: String,
        val equipment: String,
        val inventory: String,
        val spells: String,
        val manualSheet: String,
    ) {
        val values: Array<Any>
            get() = arrayOf(
                id,
                name,
                race,
                subrace,
                classes,
                background,
                abilityScores,
                proficiencies,
                equipment,
                inventory,
                spells,
                manualSheet,
            )
    }

    private companion object {
        const val INSERT_CHARACTER_SQL = """
            INSERT INTO `characters` (
                `id`, `name`, `race`, `subrace`, `classes`, `background`, `abilityScores`, `proficiencies`,
                `equipment`, `inventory`, `spells`, `manualSheet`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """

        const val SELECT_CHARACTER_SQL = """
            SELECT `id`, `name`, `race`, `subrace`, `classes`, `background`, `abilityScores`, `proficiencies`,
                `equipment`, `inventory`, `spells`, `manualSheet`
            FROM `characters`
        """
    }
}
