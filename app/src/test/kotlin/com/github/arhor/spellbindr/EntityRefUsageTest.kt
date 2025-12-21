package com.github.arhor.spellbindr

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class EntityRefUsageTest {
    @Test
    fun uiAndViewModelsAvoidDataEntityRef() {
        val uiRoot = resolveUiRoot()
        val offenders = mutableListOf<Path>()

        Files.walk(uiRoot).use { paths ->
            paths.filter { path -> path.toString().endsWith(".kt") }
                .forEach { path ->
                    val content = Files.readAllLines(path).joinToString("\n")
                    if ("com.github.arhor.spellbindr.data.model.EntityRef" in content ||
                        "data.model.EntityRef" in content
                    ) {
                        offenders.add(path)
                    }
                }
        }

        assertTrue(
            "UI/ViewModel files should not depend on data.model.EntityRef: $offenders",
            offenders.isEmpty(),
        )
    }

    private fun resolveUiRoot(): Path {
        val relativeUiPath = Path.of("app/src/main/kotlin/com/github/arhor/spellbindr/ui")
        val start = Path.of(System.getProperty("user.dir")).toAbsolutePath()

        generateSequence(start) { it.parent }
            .map { candidateRoot -> candidateRoot.resolve(relativeUiPath) }
            .firstOrNull { Files.exists(it) }
            ?.let { return it }

        val fallback = start.resolve("src/main/kotlin/com/github/arhor/spellbindr/ui")
        return fallback.takeIf { Files.exists(it) }
            ?: error("Unable to locate UI source root from $start")
    }
}
