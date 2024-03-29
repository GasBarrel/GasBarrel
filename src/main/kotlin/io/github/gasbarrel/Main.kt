@file:JvmName("Main")

package io.github.gasbarrel

import com.freya02.botcommands.api.core.BBuilder
import com.freya02.botcommands.api.core.utils.namedDefaultScope
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes
import ch.qos.logback.classic.ClassicConstants as LogbackConstants

private val logger by lazy { KotlinLogging.logger {} } // Must not load before system property is set

fun main(args: Array<out String>) {
    try {
        System.setProperty(LogbackConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info("Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}")

        // stacktrace-decoroutinator seems to have issues when reloading with hotswap agent
        when {
            "-XX:HotswapAgent=fatjar" in ManagementFactory.getRuntimeMXBean().inputArguments ->
                logger.info("Skipping stacktrace-decoroutinator as HotswapAgent is active")

            "--no-decoroutinator" in args ->
                logger.info("Skipping stacktrace-decoroutinator as --no-decoroutinator is specified")

            else -> DecoroutinatorRuntime.load()
        }

        val scope = namedDefaultScope("GasBarrel Coroutine", 4)
        val manager = CoroutineEventManager(scope, 1.minutes)
        manager.listener<ShutdownEvent> {
            scope.cancel()
        }

        val config = Config.instance

        BBuilder.newBuilder(manager) {
            if (Environment.isDev) {
                disableExceptionsInDMs = true
                disableAutocompleteCache = true
            }

            addOwners(*config.ownerIds.toLongArray())

            addSearchPath("io.github.gasbarrel")

            textCommands {
                usePingAsPrefix = "<ping>" in config.prefixes
                prefixes += config.prefixes - "<ping>"
            }

            applicationCommands {
                testGuildIds += config.testGuildIds

                addLocalizations("Commands", DiscordLocale.FRENCH)
            }

            components {
                useComponents = true
            }
        }

        logger.info("Loaded commands")
    } catch (e: Exception) {
        logger.error("Unable to start the bot", e)
        exitProcess(1)
    }
}
