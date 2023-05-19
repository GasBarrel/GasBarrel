@file:JvmName("Main")
package io.github.gasbarrel

import ch.qos.logback.classic.ClassicConstants as LogbackConstants
import com.freya02.botcommands.api.core.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.gasbarrel.utils.namedDefaultScope
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

private val logger by lazy { KotlinLogging.logger {} } // Must not load before system property is set

fun main(args: Array<out String>) {
    try {
        System.setProperty(LogbackConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info("Loading logback configuration at ${Environment.logbackConfigPath}")

        // stacktrace-decoroutinator seems to have issues when reloading with hotswap agent
        if ("-XX:HotswapAgent=fatjar" in ManagementFactory.getRuntimeMXBean().inputArguments) {
            logger.info("Skipping stacktrace-decoroutinator as HotswapAgent is active")
        } else if ("--no-decoroutinator" in args) {
            logger.info("Skipping stacktrace-decoroutinator as --no-decoroutinator is specified")
        } else {
            DecoroutinatorRuntime.load()
        }

        val scope = namedDefaultScope("GasBarrel Coroutine", 4)
        val manager = CoroutineEventManager(scope, 1.minutes)
        manager.listener<ShutdownEvent> {
            scope.cancel()
        }

        BBuilder.newBuilder(manager) {
            if (Environment.isDev) {
                disableExceptionsInDMs = true
                disableAutocompleteCache = true
            }

            addOwners(*config.ownerIds.toLongArray())

            addSearchPath("io.github.gasbarrel")

            textCommands {
                usePingAsPrefix = "@ping" in config.prefixes
                prefixes += config.prefixes
            }

            applicationCommands {
                testGuildIds += config.testGuildIds
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
