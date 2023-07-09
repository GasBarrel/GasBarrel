package io.github.gasbarrel

import com.akuleshov7.ktoml.Toml
import com.freya02.botcommands.api.core.service.annotations.BService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

@Serializable
data class DatabaseConfig(
    @SerialName("server_name")
    val serverName: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
) {
    val url: String
        get() = "jdbc:postgresql://$serverName:$port/$name"
}

@Serializable
data class Config(
    val token: String,
    val prefixes: List<String>,
    @SerialName("owner_ids")
    val ownerIds: List<Long>,
    @SerialName("test_guild_ids")
    val testGuildIds: List<Long>,
    val database: DatabaseConfig
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val configFilePath: Path = Environment.configFolder.resolve("config.toml")

        @get:BService
        val instance: Config by lazy {
            logger.info("Loading configuration at ${configFilePath.absolutePathString()}")

            Toml.decodeFromString(configFilePath.readText())
        }
    }
}
