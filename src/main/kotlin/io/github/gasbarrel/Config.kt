package io.github.gasbarrel

import com.akuleshov7.ktoml.Toml
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.suppliers.annotations.InstanceSupplier
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import kotlin.io.path.readText

@Serializable
data class DBConfig(
    val serverName: String,
    val portNumber: Int,
    val user: String,
    val password: String,
    val dbName: String
) {
    val dbURL: String
        get() = "jdbc:postgresql://$serverName:$portNumber/$dbName"
}

@BService
@Serializable
data class Config(
    val token: String,
    val ownerIds: List<Long>,
    val prefixes: List<String>,
    val testGuildIds: List<Long>,
    val dbConfig: DBConfig
) {
    companion object {
        private val logger = KotlinLogging.logger { }
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
