package io.github.gasbarrel

import java.nio.file.Path

object Data {
    val folder: Path = Environment.folder.resolve(if (Environment.isDev) "dev-data" else "data")
}
