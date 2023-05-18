package io.github.gasbarrel

import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.notExists

// If test configs exists then they should be loaded
// If not then fallback to the config path, which must be validated
// Other configs such as logback are set in the bot folder
object Data {
    private val botFolder: Path = validatedPath("Bot folder", Path(System.getProperty("user.home"), "Bots", "GasBarrel"))
    private val configFolder: Path = botFolder.resolve("config")

    //Fine if not used, might just be using the test config
    private val configPath: Path = configFolder.resolve("config.toml")
    private val testConfigPath: Path = Path("config.test.toml")

    val logbackConfigPath: Path = configFolder.resolve("logback.xml")

    fun init() {
        //Create optional folders
    }

    val isDevEnvironment = testConfigPath.exists()

    fun getEffectiveConfigPath(): Path = when {
        isDevEnvironment -> testConfigPath
        configPath.exists() -> configPath
        else -> throw FileNotFoundException("Bot config at ${configPath.absolutePathString()} does not exist and test config at ${testConfigPath.absolutePathString()} was not found either.")
    }

    private fun validatedPath(desc: String, p: Path): Path {
        if (p.notExists())
            throw FileNotFoundException("$desc at ${p.absolutePathString()} does not exist.")
        return p
    }
}