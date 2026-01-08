pluginManagement {
    repositories {
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = file("buildSrc/src/main/kotlin/Constants.kt").readLines()
    .map(String::trim)
    .find { it.startsWith("const val PROJECT_NAME") }
    ?.substringAfter('"')
    ?.substringBefore('"')
    ?: throw IllegalStateException("Failed to find mod PROJECT_NAME")
