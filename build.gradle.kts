import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "io.github.gasbarrel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.9")
    implementation("io.github.minndevelopment:jda-ktx:9fc90f616b")

    // Application commands
    implementation("io.github.freya022:BotCommands:583b02c4f0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

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
        )
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "19"
    targetCompatibility = "19"
    options.compilerArgs.add("--enable-preview") // Loom
}