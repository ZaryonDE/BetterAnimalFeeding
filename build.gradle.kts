import java.util.Properties

plugins {
    id("fabric-loom") version "1.5.8"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
}

group = "de.zaryon"

val versionFile = file("version.properties")
val versionProps = Properties()
if (versionFile.exists()) {
    versionProps.load(versionFile.inputStream())
} else {
    versionProps.setProperty("modVersion", "1.0.0")
    versionFile.outputStream().use { versionProps.store(it, null) }
}

fun incrementVersion(ver: String): String {
    val parts = ver.split(".").map { it.toInt() }.toMutableList()
    parts[2] = parts[2] + 1
    return parts.joinToString(".")
}

version = versionProps.getProperty("modVersion")
var currentVersion = version.toString()

tasks.named("build") {
    doFirst {
        val newVersion = incrementVersion(currentVersion)
        versionProps.setProperty("modVersion", newVersion)
        versionFile.outputStream().use { versionProps.store(it, null) }
        println("Version updated: $currentVersion -> $newVersion")
        currentVersion = newVersion
        project.version = newVersion
    }
}

base {
    archivesName = "BetterAnimalFeeding-1.19-Fabric"
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.19")
    mappings("net.fabricmc:yarn:1.19+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.58.0+1.19")
    modImplementation("com.terraformersmc:modmenu:4.0.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("betteranimalfeeding")
    versionNumber.set(version.toString())
    versionType.set("release")
    uploadFile.set(tasks.remapJar)
    gameVersions.addAll(listOf("1.19"))
    loaders.add("fabric")
    changelog.set(System.getenv("GITHUB_RELEASE_BODY") ?: "Automatischer Release-Build")
}