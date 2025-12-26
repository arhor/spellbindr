package com.github.arhor.spellbindr.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = ["com.github.arhor.spellbindr"],
    importOptions = [
        ImportOption.DoNotIncludeTests::class,
    ],
)
class ArchitectureRulesTest {
    @ArchTest
    val uiShouldNotDependOnDataEntityRef = noClasses()
        .that().resideInAnyPackage("..ui..")
        .should().dependOnClassesThat()
        .haveFullyQualifiedName("com.github.arhor.spellbindr.data.model.EntityRef")
        .because("UI should use the domain EntityRef, not the data model type.")

    @ArchTest
    val featureUiShouldNotDependOnDataImplementations = noClasses()
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

    @ArchTest
    val featureViewModelsAvoidPersistenceFrameworks = noClasses()
        .that().resideInAnyPackage("..ui.feature..")
        .and().haveSimpleNameEndingWith("ViewModel")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..data.local.db..",
            "androidx.room..",
            "androidx.datastore..",
            "android.database..",
        )
        .because("ViewModels should use repositories/use cases instead of persistence APIs.")

    @ArchTest
    val dataLayerShouldNotDependOnUi = noClasses()
        .that().resideInAnyPackage("..data..")
        .should().dependOnClassesThat().resideInAnyPackage("..ui..")
        .because("Data layer should remain UI-agnostic.")

    @ArchTest
    val domainLayerShouldNotDependOnDataImplementations = noClasses()
        .that().resideInAnyPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..data.local..",
            "..data.mapper..",
            "..data.repository..",
        )
        .because("Domain layer should not depend on data implementations.")

    @ArchTest
    val domainLayerShouldNotDependOnDataModelsOrAssets = noClasses()
        .that().resideInAnyPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..data.model..",
            "..data.local.assets..",
        )
        .because("Domain models should not rely on data serialization models or asset loaders.")

    @ArchTest
    val nonUiCorePackagesShouldNotDependOnComposeUi = noClasses()
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
}
