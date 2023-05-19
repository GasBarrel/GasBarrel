package io.github.gasbarrel

import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.*

// If dev configs exists then they should be loaded
// If not then fallback to the config path, which must be validated
// Other configs such as logback are set in the bot folder
object Data {
    private val devConfigPath: Path = Path("config.dev.toml")

    val isDevEnvironment = devConfigPath.exists()

    /**
     * * Dev: Project's root
     * * Prod: Folder in which the jar is in
     */
    private val botFolder: Path = getBotFolder()

    private val configFolder: Path = botFolder.resolve("config")
    private val dataFolder: Path = getDataFolder()

    //Fine if not used, might just be using the dev config
    private val prodConfigPath: Path = configFolder.resolve("config.toml")
    val prodLogbackConfigPath: Path = configFolder.resolve("logback.xml")

    fun init() {
        if (!isDevEnvironment) {
            prodConfigPath.validate("Bot config")
            prodLogbackConfigPath.validate("Logback config")
        }

        //Create optional folders
        dataFolder.createDirectories()
    }

    fun getEffectiveConfigPath(): Path = when {
        isDevEnvironment -> devConfigPath
        prodConfigPath.exists() -> prodConfigPath
        else -> throw FileNotFoundException("Bot config at '${prodConfigPath.absolutePathString()}' does not exist and dev config at '${devConfigPath.absolutePathString()}' was not found either.")
    }

    private fun Path.validate(desc: String) = this.also {
        if (this.notExists())
            throw FileNotFoundException("$desc at ${this.absolutePathString()} does not exist.")
    }

    private fun getBotFolder() = when {
        isDevEnvironment -> Path("")
        else -> {
            val jarPath = javaClass.protectionDomain.codeSource.location.toURI().toPath()
            if (jarPath.extension != "jar") {
                throw IllegalStateException("Production environment detected (no '${devConfigPath.name}'), but file at $jarPath isn't a JAR")
            }

            jarPath.parent
        }
    }

    private fun getDataFolder() = when {
        isDevEnvironment -> botFolder.resolve("dev-data")
        else -> botFolder.resolve("data")
    }
}