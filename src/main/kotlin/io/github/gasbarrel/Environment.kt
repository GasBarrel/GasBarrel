package io.github.gasbarrel

import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    /**
     * The mode is determined by checking whether if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev = Path("dev-config").exists()

    val configFilePath = Config.folder.resolve("config.toml")
    val logbackConfigPath = Config.folder.resolve("logback.xml")
}
