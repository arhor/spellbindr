package com.github.arhor.spellbindr.architecture

import com.github.arhor.spellbindr.SpellbindrApplication
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(packagesOf = [SpellbindrApplication::class], importOptions = [DoNotIncludeTests::class])
class ArchitectureRulesTest {
//
//    @ArchTest
//    fun `should check that correct layered architecture is observed`(
//        // Given
//        appClasses: JavaClasses,
//    ) {
//        // When
//        val archRule =
//            layeredArchitecture()
//                .consideringOnlyDependenciesInLayers()
//                .layer(DI).definedBy("..di..")
//                .layer(UI).definedBy("..ui..")
//                .layer(DATA).definedBy("..data..")
//                .layer(DOMAIN).definedBy("..domain..")
//                .whereLayer(DI).mayNotBeAccessedByAnyLayer()
//                .whereLayer(UI).mayOnlyBeAccessedByLayers(DI)
//                .whereLayer(DATA).mayOnlyBeAccessedByLayers(DI)
//                .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(UI, DI, DATA)
//
//        // Then
//        archRule.check(appClasses)
//    }

    @ArchTest
    fun `should check that feature UI don't depend on data implementations`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage(
                "..ui.feature..",
                "..ui.components..",
                "..ui.navigation..",
                "..ui.theme..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "..data.local..",
                "..data.mapper..",
                "..data.repository..",
            )
            .because("UI screens and components should not depend on data implementations.")

        // Then
        archRule.check(appClasses)
    }

    @ArchTest
    fun `should check that feature view models don't depend on persistence frameworks`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage("..ui.feature..")
            .and().haveSimpleNameEndingWith("ViewModel")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..data.local.db..",
                "androidx.room..",
                "androidx.datastore..",
                "android.database..",
            )
            .because("ViewModels should use repositories/use cases instead of persistence APIs.")

        // Then
        archRule.check(appClasses)
    }

    @ArchTest
    fun `should check that data layer should not depend on UI`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage(
                "..data..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "..ui..",
            )
            .because("Data layer should remain UI-agnostic.")

        // Then
        archRule.check(appClasses)
    }

    @ArchTest
    fun `should check that domain layer doesn't depend on data implementations`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage(
                "..domain..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "..data.local..",
                "..data.mapper..",
                "..data.repository..",
            )
            .because("Domain layer should not depend on data implementations.")

        // Then
        archRule.check(appClasses)
    }

    @ArchTest
    fun `should check that domain layer doesn't depend on data models or assets`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage(
                "..domain..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "..data.model..",
                "..data.local.assets..",
            )
            .because("Domain models should not rely on data serialization models or asset loaders.")

        // Then
        archRule.check(appClasses)
    }

    @ArchTest
    fun `should check that non-UI core packages don't depend on compose UI`(
        // Given
        appClasses: JavaClasses,
    ) {
        // When
        val archRule = noClasses()
            .that().resideInAnyPackage(
                "..domain..",
                "..data..",
                "..di..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "androidx.compose.animation..",
                "androidx.compose.foundation..",
                "androidx.compose.material..",
                "androidx.compose.material3..",
                "androidx.compose.ui..",
            )
            .because("Compose UI toolkit dependencies should stay in the UI layer.")

        // Then
        archRule.check(appClasses)
    }

    companion object {
        // @formatter:off
        private const val DI     = "DI"
        private const val UI     = "UI"
        private const val DATA   = "Data"
        private const val DOMAIN = "Domain"
        // @formatter:on
    }
}
