import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
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
    implementation("io.github.freya022:BotCommands:c5f13589a1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

    // Coroutine debugging
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.3.6")

    // Configs
    implementation("com.akuleshov7:ktoml-core:0.5.0")

    // SQL
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.18.0")

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
        jvmTarget = "20"
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
    sourceCompatibility = "20"
    targetCompatibility = "20"
    options.compilerArgs.add("--enable-preview") // Loom
}
