package io.github.gasbarrel

import kotlin.io.path.Path

object Data {
    val folder = Path(".", if (Config.isDevEnvironment) "dev-data" else "data")
}
