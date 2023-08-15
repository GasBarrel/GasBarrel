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
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.localize
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
        @LocalizationBundle("Commands", prefix = "tempban.options") options: AppLocalizationContext,
        @LocalizationBundle("Commands", prefix = "tempban.outputs") outputs: AppLocalizationContext,
        @LocalizationBundle("Commands", prefix = "tempban.components") components: AppLocalizationContext,
        target: User,
        duration: Duration,
        reason: String = options.localize("reason.default")
    ) {
        event.deferReply(true).queue()

        val member = event.guild.retrieveMemberOrNull(target)
        if (member != null) {
            //TODO perm checks
        }

        val existingTempBan = tempBanService.getActiveBan(event.guild, target)
        if (existingTempBan != null) {
            val overrideButton = componentsService.ephemeralButton(ButtonStyle.DANGER, components.localize("override.label")) { oneUse = true }
            val extendButton =
                componentsService.ephemeralButton(ButtonStyle.SECONDARY, components.localize("extend.label")) { oneUse = true }
            val abortButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, components.localize("abort.label")) { oneUse = true }
            val group = componentsService.newEphemeralGroup(overrideButton, extendButton, abortButton) {
                timeout(5.minutes)
            }
            event.hook.editOriginal(outputs.localize("already_banned", "expiration" to existingTempBan.expiresAt.toExpirationString()))
                .setComponents(row(overrideButton, extendButton, abortButton))
                .queue()

            val button = group.awaitAnyOrNull<ButtonEvent>()
                ?: return event.hook.editOriginal(outputs.localize("timeout"))
                    .setReplace(true)
                    .delay(5.seconds.toJavaDuration())
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()

            when (button.componentId) {
                overrideButton.id -> {
                    val expiration = tempBanService.overrideBan(existingTempBan, duration, reason)
                    event.hook.editOriginal(outputs.localize("overridden", "expiration" to expiration.toExpirationString()))
                        .setReplace(true)
                        .queue()
                }

                extendButton.id -> {
                    val expiration = tempBanService.extendBan(existingTempBan, duration)
                    event.hook.editOriginal(outputs.localize("extended", "expiration" to expiration.toExpirationString()))
                        .setReplace(true)
                        .queue()
                }
                abortButton.id -> event.hook.editOriginal(outputs.localize("aborted"))
                    .setReplace(true)
                    .delay(5.seconds.toJavaDuration())
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()

                else -> throw IllegalArgumentException("Could not match button with ID ${button.componentId}")
            }
        } else {
            val expiration = tempBanService.addBan(event.guild, target, duration, reason)
            event.hook.editOriginal(outputs.localize("success", "mention" to target.asMention, "expiration" to expiration.toExpirationString()))
                .setReplace(true)
                .queue()
        }
    }

    private fun Instant.toExpirationString() =
        "${TimeFormat.DATE_TIME_LONG.format(this)} (${TimeFormat.RELATIVE.format(this)})"

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("tempban", function = ::onSlashTempBan) {
            botPermissions += Permission.BAN_MEMBERS
            userPermissions += Permission.BAN_MEMBERS

            customOption("options")
            customOption("outputs")
            customOption("components")

            option("target")

            aggregate("duration", ::durationAggregator) {
                option("duration")

                option("durationUnit") {
                    usePredefinedChoices = true
                }
            }

            option("reason")
        }
    }

    @CommandMarker
    fun durationAggregator(duration: Long, durationUnit: DurationUnit): Duration {
        return duration.toDuration(durationUnit)
    }
}
