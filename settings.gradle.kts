pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.terraformersmc.com/")
    }
}
rootProject.name = "BetterAnimalFeeding"