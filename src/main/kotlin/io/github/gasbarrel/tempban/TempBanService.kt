package io.github.gasbarrel.tempban

import com.freya02.botcommands.api.core.service.annotations.BService
import io.github.gasbarrel.entities.TempBan
import io.github.gasbarrel.repositories.TempBanRepository
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@BService
class TempBanService(private val tempBanRepository: TempBanRepository) {
    fun getActiveBan(guild: Guild, user: UserSnowflake): TempBan? {
        val ban = tempBanRepository.getByMemberOrNull(guild.idLong, user.idLong) ?: return null

        return when {
            ban.expiresAt > Instant.now() -> ban
            else -> null
        }
    }

    fun addBan(guild: Guild, target: User, duration: Duration, reason: String): Instant {
        val expiration = computeExpiration(duration)
        tempBanRepository.add(guild.idLong, target.idLong, expiration, reason)

        return expiration
    }

    fun overrideBan(tempBan: TempBan, duration: Duration, reason: String): Instant {
        val expiration = computeExpiration(duration)
        tempBan.expiresAt = expiration
        tempBan.reason = reason

        tempBanRepository.update(tempBan)

        return expiration
    }

    fun extendBan(tempBan: TempBan, duration: Duration): Instant {
        val expiration = tempBan.expiresAt + duration.toJavaDuration()
        tempBan.expiresAt = expiration

        tempBanRepository.update(tempBan)

        return expiration
    }

    private fun computeExpiration(duration: Duration): Instant {
        return Instant.now() + duration.toJavaDuration()
    }
}
