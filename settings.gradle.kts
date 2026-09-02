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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Readify"

include(":app")
include(":core:model")
include(":core:rules")
include(":core:extractor")
include(":core:network")
include(":core:database")
include(":core:export")
include(":feature:library")
include(":feature:sources")
include(":feature:reader")
include(":feature:rules")
