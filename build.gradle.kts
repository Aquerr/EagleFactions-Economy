import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
}

group = "io.github.aquerr"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = uri("https://jitpack.io"))
}

sponge {
    apiVersion("8.2.0")
    license("All-Rights-Reserved")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("eaglefactions-economy") {
        displayName("EagleFactions-Economy")
        entrypoint("io.github.aquerr.eaglefactionseconomy.EagleFactionsEconomy")
        description("Economy add-on for EagleFactions.")
        links {
             homepage("https://github.com/Aquerr/EagleFactions-Economy")
             source("https://github.com/Aquerr/EagleFactions-Economy")
             issues("https://github.com/Aquerr/EagleFactions-Economy/issues")
        }
        contributor("Aquerr") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        dependency("eaglefactions") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("^1.0.1")
        }
    }
}

dependencies {
    implementation("com.github.Aquerr:EagleFactionsAPI:v1.0.1")
}

val javaTarget = 11
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
