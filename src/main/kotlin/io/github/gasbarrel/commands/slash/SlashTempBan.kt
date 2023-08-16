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
import com.freya02.botcommands.api.core.utils.delay
import com.freya02.botcommands.api.core.utils.replaceWith
import com.freya02.botcommands.api.core.utils.retrieveMemberOrNull
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.context.AppLocalizationContext
import com.freya02.botcommands.api.localization.context.localize
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.MessageCreate
import io.github.gasbarrel.entities.TempBan
import io.github.gasbarrel.tempban.TempBanService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import java.awt.Color
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Command
class SlashTempBan(
    private val tempBanService: TempBanService,
    private val componentsService: Components
) : ApplicationCommand() {

    private enum class Decision {
        OVERRIDE, EXTEND, ABORT
    }

    private val banGifs = listOf(
        "https://media.tenor.com/N_8SL-Rl-6EAAAAC/survival-game-club-anime.gif",
        "https://media.tenor.com/dpeTg8S0ogYAAAAC/gun-die.gif",
        "https://media.tenor.com/C_h9TiM0AMUAAAAC/anime-girl.gif"
    )

    @CommandMarker
    suspend fun onSlashTempBan(
        event: GuildSlashEvent,
        @LocalizationBundle("Commands", prefix = "tempban.options") options: AppLocalizationContext,
        @LocalizationBundle("Commands", prefix = "tempban.outputs") outputs: AppLocalizationContext,
        @LocalizationBundle("Commands", prefix = "tempban.embed_parts") embedParts: AppLocalizationContext,
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
            val decision = awaitUserDecision(event, components, outputs, existingTempBan)
            if (tempBanService.getActiveBan(event.guild, target)?.id != existingTempBan.id ) {
                return event.hook
                    .replaceWith(outputs.localize("expired"))
                    .delay(10.seconds)
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()
            }

            when (decision) {
                Decision.OVERRIDE -> {
                    val tempBan = tempBanService.overrideBan(existingTempBan, duration, reason)
                    event.hook
                        .replaceWith(createMessage(embedParts, "titles.overridden", target, tempBan.expiresAt, tempBan.reason))
                        .queue()
                }

                Decision.EXTEND -> {
                    val tempBan = tempBanService.extendBan(existingTempBan, duration)
                    event.hook
                        .replaceWith(createMessage(embedParts, "titles.extended", target, tempBan.expiresAt, tempBan.reason))
                        .queue()
                }

                Decision.ABORT -> event.hook
                    .replaceWith(outputs.localize("aborted"))
                    .delay(5.seconds)
                    .flatMap { event.hook.deleteOriginal() }
                    .queue()
            }
        } else {
            val expiration = tempBanService.addBan(event.guild, target, duration, reason)
            event.hook
                .replaceWith(createMessage(embedParts, "titles.success", target, expiration, reason))
                .queue()
        }
    }

    private suspend fun awaitUserDecision(
        event: GuildSlashEvent,
        components: AppLocalizationContext,
        outputs: AppLocalizationContext,
        existingTempBan: TempBan
    ): Decision {
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

        val button = group.awaitAnyOrNull<ButtonEvent>() ?: return Decision.ABORT
        return when (button.componentId) {
            overrideButton.id -> Decision.OVERRIDE
            extendButton.id -> Decision.EXTEND
            abortButton.id -> Decision.ABORT
            else -> throw IllegalArgumentException("Could not match button with ID ${button.componentId}")
        }
    }

    //TODO publish to mod channel

    private fun createMessage(
        embedParts: AppLocalizationContext,
        titleKey: String,
        target: UserSnowflake,
        expiration: Instant,
        reason: String
    ) = MessageCreate {
        embed {
            title = embedParts.localize(titleKey)

            color = Color.RED.rgb

            field {
                name = embedParts.localize("field.member.name")
                value = target.asMention
                inline = true
            }

            field {
                name = embedParts.localize("field.expires.name")
                value = expiration.toExpirationString()
                inline = true
            }

            field {
                name = embedParts.localize("field.reason.name")
                value = reason
            }

            image = banGifs.random()
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
            customOption("embedParts")
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
