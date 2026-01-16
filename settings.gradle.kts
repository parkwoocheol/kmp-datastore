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
rootProject.name = "kmp-datastore"

// Library module
include(":kmp-datastore")

// Sample modules
include(":sample:shared")
include(":sample:androidApp")
include(":sample:desktopApp")
