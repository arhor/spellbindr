package com.github.arhor.spellbindr.architecture

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence

class ModuleDependencyRulesTest {

    @Test
    fun `feature modules should not depend on data or app modules`() {
        val projectRoot = resolveProjectRoot()
        featureBuildFiles(projectRoot).forEach { path ->
            val content = readFile(path)
            assertThat(content.contains("project(\":data:")).isFalse()
            assertThat(content.contains("project(\":app")).isFalse()
        }
    }

    @Test
    fun `core domain module should not depend on data, feature, or app modules`() {
        val projectRoot = resolveProjectRoot()
        coreDomainBuildFiles(projectRoot).forEach { path ->
            val content = readFile(path)
            assertThat(content.contains("project(\":data:")).isFalse()
            assertThat(content.contains("project(\":feature:")).isFalse()
            assertThat(content.contains("project(\":app")).isFalse()
        }
    }

    @Test
    fun `data modules should not depend on feature or app modules`() {
        val projectRoot = resolveProjectRoot()
        dataBuildFiles(projectRoot).forEach { path ->
            val content = readFile(path)
            assertThat(content.contains("project(\":feature:")).isFalse()
            assertThat(content.contains("project(\":app")).isFalse()
        }
    }

    private fun featureBuildFiles(projectRoot: Path): List<Path> =
        buildFilesUnder(projectRoot.resolve("feature"))

    private fun coreDomainBuildFiles(projectRoot: Path): List<Path> =
        buildFilesUnder(projectRoot.resolve("core").resolve("domain"))

    private fun dataBuildFiles(projectRoot: Path): List<Path> =
        buildFilesUnder(projectRoot.resolve("data"))

    private fun buildFilesUnder(root: Path): List<Path> {
        if (!Files.exists(root)) return emptyList()

        return Files.walk(root)
            .use { paths ->
                paths
                    .asSequence()
                    .filter { Files.isRegularFile(it) }
                    .filter { it.fileName.toString() == "build.gradle.kts" }
                    .toList()
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

    private fun readFile(path: Path): String =
        Files.newBufferedReader(path).use { it.readText() }
}
