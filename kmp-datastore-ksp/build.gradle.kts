plugins {
    kotlin("jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.ksp.api)
    implementation(project(":kmp-datastore-annotations"))
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.parkwoocheol"
            artifactId = "kmp-datastore-ksp"
            version = "1.0.0"
            from(components["java"])

            pom {
                name.set("KMP DataStore KSP Processor")
                description.set("KSP processor for generating type-safe query builders and validators")
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
