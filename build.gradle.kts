import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application

    id("com.github.ben-manes.versions") version "0.47.0"

    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.0"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.9.0"
}

group = "io.github.gasbarrel"
version = "1.0-SNAPSHOT"

allOpen {
    annotations(
        "jakarta.persistence.Entity",
        "jakarta.persistence.Embeddable",
        "jakarta.persistence.Column"
    )
}

noArg {
    annotations(
        "jakarta.persistence.Entity",
        "jakarta.persistence.Embeddable"
    )
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.13")
    implementation("io.github.minndevelopment:jda-ktx:9fc90f616b")

    // Application commands
    implementation("io.github.freya022:BotCommands:e3b57ae397")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Coroutine debugging
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.3.6")

    // Configs
    implementation("com.akuleshov7:ktoml-core:0.5.0")

    // HOCON
    implementation("com.typesafe:config:1.4.2")

    // SQL
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.21.1")
    implementation("org.hibernate.orm:hibernate-core:6.2.7.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.2.7.Final")

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
