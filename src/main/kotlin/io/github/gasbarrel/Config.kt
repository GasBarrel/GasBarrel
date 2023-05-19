package io.github.gasbarrel

import com.akuleshov7.ktoml.Toml
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import kotlin.io.path.readText

@Serializable
data class DatabaseConfig(
    val serverName: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
) {
    val url: String = "jdbc:postgresql://$serverName:$port/$name"
}

@BService
@Serializable
data class Config(
    val token: String,
    val prefixes: List<String>,
    val ownerIds: List<Long>,
    val testGuildIds: List<Long>,
    val database: DatabaseConfig
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        val config: Config by lazy {
            val configPath = Data.getEffectiveConfigPath()

            if (Data.isDevEnvironment) {
                logger.info("Loading test config")
            }

            return@lazy Toml.decodeFromString(configPath.readText())
        }

        @InstanceSupplier
        fun supply(): Config = config
    }
}
