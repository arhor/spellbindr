package com.github.arhor.spellbindr.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchUnitImportTest {
    @Test
    fun `ClassFileImporter should include production packages when configured to exclude tests`() {
        // Given
        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.github.arhor.spellbindr")

        // When
        val packageNames = importedClasses.map { it.packageName }

        // Then
        assertTrue(
            "Expected production classes to be imported, but none were found.",
            importedClasses.size > 0,
        )
        assertTrue(
            "Expected UI feature classes to be present in imported classes.",
            packageNames.any { it.startsWith("com.github.arhor.spellbindr.ui.feature") },
        )
        assertTrue(
            "Expected domain classes to be present in imported classes.",
            packageNames.any { it.startsWith("com.github.arhor.spellbindr.domain") },
        )
    }
}
