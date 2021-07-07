import kr.entree.spigradle.kotlin.papermc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.32"
    id("com.diffplug.spotless") version "5.14.1"
    id("kr.entree.spigradle") version "2.2.3"
    id("io.freefair.lombok") version "5.3.0"
}

group = "com.dumbdogdiner"
version = "4.2.0"

allprojects {
    // Declare global repositories
    repositories {
        jcenter()
        mavenCentral()

        // Add paper repository here, as it's used in both API and Bukkit modules.
        papermc()
    }
}

subprojects {
    group = "com.dumbdogdiner.stickycommands"

    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "kr.entree.spigradle")

    // Spotless configuration
    apply(plugin =  "com.diffplug.spotless")
    spotless {
        ratchetFrom = "origin/master"
    }

    repositories {
        jcenter()
        mavenCentral()
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    tasks.withType<JavaCompile> {
        targetCompatibility = JavaVersion.VERSION_11.toString()
        sourceCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

tasks {
    // Disable root project building spigot description.
    generateSpigotDescription {
        enabled = false
    }
}