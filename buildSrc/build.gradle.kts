plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
    mavenCentral()
}
