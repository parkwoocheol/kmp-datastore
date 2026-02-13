plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KmpDataStoreSerializerKotlinx"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp-datastore"))
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.parkwoocheol.kmpdatastore.serializer.kotlinx"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()

    if (project.hasProperty("signingInMemoryKey")) {
        signAllPublications()
    }

    coordinates(group.toString(), "kmp-datastore-serializer-kotlinx", version.toString())

    pom {
        name.set("KMP DataStore Kotlinx Serializer")
        description.set("Optional Kotlinx Serialization module for KMP DataStore")
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
