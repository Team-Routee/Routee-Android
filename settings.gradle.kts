pluginManagement {
    includeBuild("build-logic")
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Routee-Android"
include(":app")

include(":core:common")
include(":core:designsystem")
include(":core:data")
include(":core:network")
include(":core:navigation")

include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:record-edit:api")
include(":feature:record-edit:impl")
include(":feature:timeline:api")
include(":feature:timeline:impl")
include(":feature:archive:api")
include(":feature:archive:impl")
include(":feature:workout:api")
include(":feature:workout:impl")
include(":feature:setting:api")
include(":feature:setting:impl")
