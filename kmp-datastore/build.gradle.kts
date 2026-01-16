plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
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
            baseName = "KmpDataStore"
            isStatic = true
        }
    }

    // JVM (Desktop) target
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
        }

        androidUnitTest.dependencies {
            implementation(libs.androidx.test.core)
            implementation(libs.robolectric)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }

        iosMain.dependencies {}

        val desktopMain by getting {
            dependencies {}
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
            }
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes("*.platform.*")
            }
        }
    }
}

android {
    namespace = "com.parkwoocheol.kmpdatastore"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

// Publishing configuration for JitPack and GitHub Packages
publishing {
    publications {
        // Kotlin Multiplatform automatically creates publications for each target
        // We just need to configure the common properties
        withType<MavenPublication> {
            groupId = "com.github.parkwoocheol"
            version = "0.1.0"

            pom {
                name.set("KMP DataStore")
                description.set("Type-safe Kotlin Multiplatform DataStore wrapper with BridgeSerializer pattern")
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
                        name.set("Park Woo Cheol")
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

    // Optional: GitHub Packages repository
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/parkwoocheol/kmp-datastore")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
