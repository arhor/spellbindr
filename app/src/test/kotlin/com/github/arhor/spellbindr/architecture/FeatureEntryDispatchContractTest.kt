package com.github.arhor.spellbindr.architecture

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.file.Files
import java.nio.file.LinkOption
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
        val entryScreens = findEntryScreens(projectRoot)

        assertThat(entryScreens).isNotEmpty()

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

    private fun findEntryScreens(projectRoot: Path): List<FeatureEntryScreen> {
        return FEATURE_ROOTS.asSequence()
            .map(projectRoot::resolve)
            .filter { Files.exists(it, LinkOption.NOFOLLOW_LINKS) }
            .flatMap { featureRoot ->
                Files.walk(featureRoot)
                    .use { paths ->
                        paths
                            .filter { Files.isRegularFile(it) }
                            .map(featureRoot::relativize)
                            .map { it.toString().replace('\\', '/') }
                            .filter { it.endsWith("Screen.kt") }
                            .filter { !it.contains("/components/") }
                            .map { relativePath ->
                                FeatureEntryScreen(
                                    path = "${featureRoot.toAbsolutePath().normalize()}/$relativePath",
                                    functionName = relativePath.substringAfterLast('/').removeSuffix(".kt"),
                                )
                            }
                            .toList()
                    }.asSequence()
            }
            .filter { it.path !in IGNORED_SCREEN_PATHS }
            .sortedBy(FeatureEntryScreen::path)
            .toList()
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

    companion object {
        private val FEATURE_ROOTS = listOf(
            "feature/character/src/main/kotlin/com/github/arhor/spellbindr/ui/feature",
            "feature/compendium/src/main/kotlin/com/github/arhor/spellbindr/ui/feature",
            "feature/dice/src/main/kotlin/com/github/arhor/spellbindr/ui/feature",
            "feature/settings/src/main/kotlin/com/github/arhor/spellbindr/ui/feature",
        )
        private val IGNORED_SCREEN_PATHS: Set<String> = emptySet()
    }
}
