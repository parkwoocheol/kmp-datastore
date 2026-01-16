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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kmp-datastore"

// Library modules
include(":kmp-datastore")
include(":kmp-datastore-annotations")
include(":kmp-datastore-ksp")

// Sample modules
include(":sample:shared")
include(":sample:androidApp")
include(":sample:desktopApp")
