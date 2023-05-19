package io.github.gasbarrel

import kotlin.io.path.Path

object Data {
    val folder = Path(".", if (Environment.isDev) "dev-data" else "data")
}
