package io.github.gasbarrel

import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.service.annotations.BService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import java.sql.Connection
import kotlin.time.Duration.Companion.seconds

@BService
class DatabaseSource(config: Config) : ConnectionSupplier {
    private val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.database.url
        username = config.database.user
        password = config.database.password

        maximumPoolSize = 2
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        //Migrate BC tables
        createFlyway("bc", "bc_database_scripts").migrate()

        //Migrate gas barrels
        createFlyway("gasbarrel", "database_scripts").migrate()

        logger.info("Created database source")
    }

    override fun getMaxConnections(): Int = source.maximumPoolSize

    override fun getConnection(): Connection = source.connection

    private fun createFlyway(schema: String, scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(source)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
