package io.github.gasbarrel.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.awaitAnyOrNull
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.core.utils.retrieveMemberOrNull
import dev.minn.jda.ktx.interactions.components.row
import io.github.gasbarrel.tempban.TempBanService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Command
class SlashTempBan(
    private val tempBanService: TempBanService,
    private val componentsService: Components
) : ApplicationCommand() {

    @CommandMarker
    suspend fun onSlashTempBan(
        event: GuildSlashEvent,
        target: User,
        duration: Duration,
        reason: String = "No reason supplied" //TODO localization
    ) {
        event.deferReply(true).queue()

        val member = event.guild.retrieveMemberOrNull(target)
        if (member != null) {
            //TODO perm checks
        }

        val existingTempBan = tempBanService.getActiveBan(event.guild, target)
        if (existingTempBan != null) {
            //TODO localization
            val overrideButton = componentsService.ephemeralButton(ButtonStyle.DANGER, "Override") { oneUse = true }
            val extendButton =
                componentsService.ephemeralButton(ButtonStyle.SECONDARY, "Extend (discards reason)") { oneUse = true }
            val abortButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Abort") { oneUse = true }
            val group = componentsService.newEphemeralGroup(overrideButton, extendButton, abortButton) {
                timeout(5.minutes)
            }
            //TODO localization
            event.hook.editOriginal("This user is already temporarily banned until ${existingTempBan.expiresAt.toExpirationString()}")
                .setComponents(row(overrideButton, extendButton, abortButton))
                .queue()

            val button = group.awaitAnyOrNull<ButtonEvent>()
            //TODO localization, better response
                ?: return event.hook.editOriginal("Timeout reached")
                    .setReplace(true)
                    .delay(5.seconds.toJavaDuration())
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()

            when (button.componentId) {
                overrideButton.id -> {
                    val expiration = tempBanService.overrideBan(existingTempBan, duration, reason)
                    //TODO localization, better response
                    event.hook.editOriginal("Successfully override temp ban, expiring at ${expiration.toExpirationString()}")
                        .setReplace(true)
                        .queue()
                }

                extendButton.id -> {
                    val expiration = tempBanService.extendBan(existingTempBan, duration)
                    //TODO localization, better response
                    event.hook.editOriginal("Successfully extended temp ban, expiring at ${expiration.toExpirationString()}")
                        .setReplace(true)
                        .queue()
                }
                //TODO localization, better response
                abortButton.id -> event.hook.editOriginal("Aborted temp ban")
                    .setReplace(true)
                    .delay(5.seconds.toJavaDuration())
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()

                else -> throw IllegalArgumentException("Could not match button with ID ${button.componentId}")
            }
        } else {
            val expiration = tempBanService.addBan(event.guild, target, duration, reason)
            //TODO localization, better response
            event.hook.editOriginal("Successfully added temp ban, expiring at ${expiration.toExpirationString()}")
                .setReplace(true)
                .queue()
        }
    }

    private fun Instant.toExpirationString() =
        "${TimeFormat.DATE_TIME_LONG.format(this)} (${TimeFormat.RELATIVE.format(this)})"

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        //TODO localization
        manager.slashCommand("tempban", function = ::onSlashTempBan) {
            description = "Bans an user temporarily"

            botPermissions += Permission.BAN_MEMBERS
            userPermissions += Permission.BAN_MEMBERS

            option("target") {
                description = "The user to ban temporarily"
            }

            aggregate("duration", ::durationAggregator) {
                option("duration") {
                    description = "The duration of the temp ban"
                }

                option("durationUnit") {
                    description = "The unit of the temp ban duration"
                    usePredefinedChoices = true
                }
            }

            option("reason") {
                description = "The reason of the temp ban"
            }
        }
    }

    @CommandMarker
    fun durationAggregator(duration: Long, durationUnit: DurationUnit): Duration {
        return duration.toDuration(durationUnit)
    }
}
