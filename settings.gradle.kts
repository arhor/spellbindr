@file:Suppress("UnstableApiUsage")

include(":data:classes")


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

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Spellbindr"

include(":app")

include(":core:assets")
include(":core:components")
include(":core:logging")
include(":core:theme")
include(":core:utils")

include(":data:common")
include(":data:conditions")
include(":data:spells")

include(":features:spells")
