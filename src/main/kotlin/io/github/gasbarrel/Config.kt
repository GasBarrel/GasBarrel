package io.github.gasbarrel

import com.akuleshov7.ktoml.Toml
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

@Serializable
data class DatabaseConfig(
    @SerialName("server-name")
    val serverName: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
) {
    val url: String
        get() = "jdbc:postgresql://$serverName:$port/$name"
}

@BService
@Serializable
data class Config(
    val token: String,
    val prefixes: List<String>,
    @SerialName("owner-ids")
    val ownerIds: List<Long>,
    @SerialName("test-guild-ids")
    val testGuildIds: List<Long>,
    val database: DatabaseConfig
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        val folder: Path = Path(".", if (Environment.isDev) "dev-config" else "config")

        val instance: Config by lazy {
            logger.info("Loading configuration at ${Environment.configFilePath}")

            Toml.decodeFromString(Environment.configFilePath.readText())
        }

        @InstanceSupplier
        fun supply(): Config = instance
    }
}
