package io.github.gasbarrel.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.utils.enumSetOf
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit

@Resolver
class DurationUnitResolver :
    ParameterResolver<DurationUnitResolver, DurationUnit>(DurationUnit::class),
    SlashParameterResolver<DurationUnitResolver, DurationUnit> {

    override val optionType: OptionType = OptionType.STRING

    override fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return enumSetOf(DurationUnit.MINUTES, DurationUnit.HOURS, DurationUnit.DAYS).map { Choice(it.toTimeUnit().toChronoUnit().toString(), it.name) }
    }

    override suspend fun resolveSuspend(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): DurationUnit {
        return enumValueOf<DurationUnit>(optionMapping.asString)
    }
}
