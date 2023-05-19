package io.github.gasbarrel

import java.nio.file.Path
import kotlin.io.path.Path

object Data {
    val folder: Path = Path(if (Environment.isDev) "dev-data" else "data")
}
