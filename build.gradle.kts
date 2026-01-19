// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.spotless)
}

// Configuration for JitPack publishing
// Configuration for JitPack publishing
// group and version Moved to allprojects block

allprojects {
    group = "com.github.parkwoocheol"
    version = System.getenv("KMP_DATASTORE_VERSION") ?: "1.0.0"

    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            // Exclude build outputs and sample apps (TODO: fix and re-enable sample apps)
            targetExclude(
                "**/build/**/*.kt",
                "**/sample/**/*.kt",
            )
            ktlint()
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
