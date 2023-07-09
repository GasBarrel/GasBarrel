package io.github.gasbarrel.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_

@Command
class SlashPing {
    @CommandMarker
    suspend fun onSlashPing(event: GlobalSlashEvent) {
        event.jda.restPing
            .await()
            .let { event.reply_("Pong! $it ms", ephemeral = true) }
            .queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("ping", scope = CommandScope.GLOBAL, ::onSlashPing) {
            description = "Pong!"
        }
    }
}
