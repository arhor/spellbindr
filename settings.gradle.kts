@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Spellbindr"

include(":app")

include(":core:common")
include(":core:common-android")
include(":core:domain")
include(":core:ui")
include(":core:ui-spells")

include(":data:character")
include(":data:compendium")
include(":data:preferences")

include(":feature:character")
include(":feature:compendium")
include(":feature:dice")
include(":feature:settings")
