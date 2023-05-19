package io.github.gasbarrel

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.toPath

object Environment {
    /**
     * The mode is determined by checking whether if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = Path("dev-config").exists()

    /**
     * The folder where the data and configuration directories reside.
     */
    val folder: Path = when {
        isDev -> Path(".")
        else -> {
            val jarPath = javaClass.protectionDomain.codeSource.location.toURI().toPath()
            if (jarPath.extension != "jar") {
                throw IllegalStateException("Production environment detected (no 'dev-config' folder), but file at $jarPath isn't a JAR")
            }

            jarPath.parent
        }
    }

    val logbackConfigPath: Path = Config.folder.resolve("logback.xml")
}
