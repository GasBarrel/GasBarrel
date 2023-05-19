package io.github.gasbarrel

import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import java.nio.file.Path
import java.sql.Connection
import kotlin.io.path.*
import kotlin.time.Duration.Companion.seconds

private val migrationNameRegex = Regex("""v(\d+)\.(\d+)__.+\.sql""")
private val databaseVersionRegex = Regex("""(\d+)\.(\d+)""")

@BService(ServiceStart.PRE_LOAD)
@ServiceType(type = ConnectionSupplier::class)
class DatabaseSource(config: Config) : ConnectionSupplier {
    private val version = "1.0" // Same version as in CreateDatabase.sql

    private val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.database.url
        username = config.database.user
        password = config.database.password

        maximumPoolSize = 2
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        checkVersion()

        logger.info("Created database source")
    }

    private fun checkVersion() {
        source.connection.use { connection ->
            connection.prepareStatement("SELECT version FROM gasbarrel_version").use { statement ->
                statement.executeQuery().use { rs ->
                    if (!rs.next()) throw IllegalStateException("Found no version in database, please refer to the README to set up the database")

                    val databaseVersion = rs.getString("version")

                    if (databaseVersion != version) {
                        val sqlFolderPath = Path("sql")

                        val suffix = if (sqlFolderPath.exists())
                            buildHintSuffix(sqlFolderPath, databaseVersion)
                        else
                            ""

                        throw IllegalStateException("Database version mismatch, expected $version, got $databaseVersion $suffix")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun buildHintSuffix(sqlFolderPath: Path, databaseVersion: String): String {
        val hintFiles = sqlFolderPath.walk()
            .filter { it.extension == "sql" }
            .filter {
                val (_, major, minor) = migrationNameRegex.matchEntire(it.name)?.groupValues
                    ?: return@filter false
                val (_, dbMajor, dbMinor) = databaseVersionRegex.matchEntire(databaseVersion)?.groupValues
                    ?: return@filter false

                // Keep if db version is lower than file
                if (dbMajor.toInt() < major.toInt()) return@filter true
                if (dbMinor.toInt() < minor.toInt()) return@filter true

                return@filter false
            }
            .joinToString { it.name }

        if (hintFiles.isBlank()) return ""
        return "\nHint: You should run the following migration scripts: $hintFiles"
    }

    override fun getMaxConnections(): Int = source.maximumPoolSize

    override fun getConnection(): Connection = source.connection

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
