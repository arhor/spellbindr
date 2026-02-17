package com.github.arhor.spellbindr.architecture

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8

class FeatureEntryDispatchContractTest {

    private data class FeatureEntryScreen(
        val path: String,
        val functionName: String,
    )

    @Test
    fun `feature entry screens should expose dispatch and not expose feature onX callbacks`() {
        val projectRoot = resolveProjectRoot()
        val entryScreens = listOf(
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/settings/SettingsScreen.kt",
                functionName = "SettingsScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/alignments/AlignmentsScreen.kt",
                functionName = "AlignmentsScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/conditions/ConditionsScreen.kt",
                functionName = "ConditionsScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/races/RacesScreen.kt",
                functionName = "RacesScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/spells/SpellsScreen.kt",
                functionName = "SpellsScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/spelldetails/SpellDetailsScreen.kt",
                functionName = "SpellDetailScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/compendium/CompendiumScreen.kt",
                functionName = "CompendiumScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/dice/DiceRollerScreen.kt",
                functionName = "DiceRollerScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/list/CharactersListScreen.kt",
                functionName = "CharactersListScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/spellpicker/CharacterSpellPickerScreen.kt",
                functionName = "CharacterSpellPickerScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/editor/CharacterEditorScreen.kt",
                functionName = "CharacterEditorScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/GuidedCharacterSetupScreen.kt",
                functionName = "GuidedCharacterSetupScreen",
            ),
            FeatureEntryScreen(
                path = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/sheet/CharacterSheetScreen.kt",
                functionName = "CharacterSheetScreen",
            ),
        )

        entryScreens.forEach { screen ->
            val filePath = projectRoot.resolve(screen.path)
            val source = String(Files.readAllBytes(filePath), UTF_8)
            val signature = Regex(
                pattern = "fun\\s+${screen.functionName}\\s*\\((.*?)\\)",
                options = setOf(RegexOption.DOT_MATCHES_ALL),
            ).find(source)

            assertThat(signature).isNotNull()

            val params = signature!!.groupValues[1]
            val featureOnCallbacksCount = Regex("\\bon[A-Z][A-Za-z0-9_]*\\s*:").findAll(params).count()
            val hasDispatchParam = Regex("\\bdispatch\\s*:").containsMatchIn(params)

            assertThat(hasDispatchParam).isTrue()
            assertThat(featureOnCallbacksCount).isEqualTo(0)
        }
    }

    private fun resolveProjectRoot(): Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (true) {
            if (Files.exists(current.resolve("settings.gradle.kts"))) {
                return current
            }
            val parent = current.parent ?: return current
            current = parent
        }
    }
}
