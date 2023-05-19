package io.github.gasbarrel

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    /**
     * The mode is determined by checking whether if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = Path("dev-config").exists()

    val logbackConfigPath: Path = Config.folder.resolve("logback.xml")
}
