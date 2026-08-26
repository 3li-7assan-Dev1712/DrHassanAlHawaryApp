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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "HassanAlHawary"
include(":app")
include(":admin")
include(":feature:feature-splash-screen")
include(":core:core-domain")
include(":data")
include(":core:core-database")
include(":core:core-network")
include(":feature:feature-video")
include(":feature:feature-home")
include(":feature:feature-onboarding")
include(":feature:feature-audio")
include(":core:core-ui")
include(":feature:feature-image")
include(":feature:feature-article")
include(":feature:feature-about-dr-hassan")
include(":feature:feature-auth")
include(":feature:feature-profile")
include(":feature:feature-search")
include(":feature:feature-study")
include(":core:core-player")
include(":feature:feature-share")
