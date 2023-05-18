import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

group = "io.github.gasbarrel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("io.github.gasbarrel.Main")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = "18"
        freeCompilerArgs += "-Xcontext-receivers"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "18"
    targetCompatibility = "18"
    options.compilerArgs.add("--enable-preview") // Loom
}