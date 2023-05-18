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
        jvmTarget = "19"
        freeCompilerArgs += listOf(
            "-Xcontext-receivers",
            "-java-parameters",
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-Xskip-prerelease-check",
            "-Xlambdas=indy"
        )
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "19"
    targetCompatibility = "19"
    options.compilerArgs.add("--enable-preview") // Loom
}