package com.github.arhor.spellbindr.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchUnitImportTest {
    @Test
    fun importsProductionClasses() {
        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.github.arhor.spellbindr")

        assertTrue(
            "Expected production classes to be imported, but none were found.",
            importedClasses.size > 0,
        )
        assertTrue(
            "Expected UI feature classes to be present in imported classes.",
            importedClasses.any { it.packageName.startsWith("com.github.arhor.spellbindr.ui.feature") },
        )
        assertTrue(
            "Expected domain classes to be present in imported classes.",
            importedClasses.any { it.packageName.startsWith("com.github.arhor.spellbindr.domain") },
        )
    }
}
