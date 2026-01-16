plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("maven-publish")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KmpDataStoreAnnotations"
            isStatic = true
        }
    }

    // JVM (Desktop) target
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            // No dependencies - pure annotations
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.parkwoocheol.kmpdatastore.annotations"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Publishing configuration
publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.parkwoocheol"
            version = "1.0.0"

            pom {
                name.set("KMP DataStore Annotations")
                description.set("Annotation definitions for KMP DataStore")
                url.set("https://github.com/parkwoocheol/kmp-datastore")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("parkwoocheol")
                        name.set("Woocheol Park")
                        url.set("https://github.com/parkwoocheol")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/parkwoocheol/kmp-datastore.git")
                    developerConnection.set("scm:git:ssh://github.com/parkwoocheol/kmp-datastore.git")
                    url.set("https://github.com/parkwoocheol/kmp-datastore")
                }
            }
        }
    }
}
