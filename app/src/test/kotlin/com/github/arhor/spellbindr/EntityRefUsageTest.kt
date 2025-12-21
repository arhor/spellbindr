package com.github.arhor.spellbindr

import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class EntityRefUsageTest {
    @Test
    fun uiAndViewModelsAvoidDataEntityRef() {
        val uiRoot = Path.of("app/src/main/kotlin/com/github/arhor/spellbindr/ui")
        val offenders = mutableListOf<Path>()

        Files.walk(uiRoot).use { paths ->
            paths.filter { path -> path.toString().endsWith(".kt") }
                .forEach { path ->
                    val content = Files.readString(path)
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
}
