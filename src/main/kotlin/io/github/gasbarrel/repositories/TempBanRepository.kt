package io.github.gasbarrel.repositories

import com.freya02.botcommands.api.core.service.annotations.BService
import io.github.gasbarrel.entities.TempBan
import io.github.gasbarrel.providers.withEntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.NoResultException
import java.time.Instant

@BService
class TempBanRepository(private val entityManagerFactory: EntityManagerFactory) {
    fun getByMember(guildId: Long, userId: Long): TempBan = entityManagerFactory.withEntityManager {
        query<TempBan>("""
                SELECT tempBan
                FROM TempBan tempBan
                WHERE tempBan.guildId = :guildId 
                  AND tempBan.userId = :userId
                ORDER BY tempBan.expiresAt DESC
                LIMIT 1
            """.trimIndent()) {
            setParameter("guildId", guildId)
            setParameter("userId", userId)
        }.singleResult
    }

    fun getByMemberOrNull(guildId: Long, userId: Long): TempBan? {
        return try {
            getByMember(guildId, userId)
        } catch (e: NoResultException) {
            null
        }
    }

    fun add(guildId: Long, userId: Long, expiration: Instant, reason: String): Unit = entityManagerFactory.withEntityManager {
        createNativeQuery("INSERT INTO temp_ban (guild_id, user_id, expires_at, reason) VALUES (?, ?, ?, ?)")
            .setParameter(1, guildId)
            .setParameter(2, userId)
            .setParameter(3, expiration)
            .setParameter(4, reason)
            .executeUpdate()
    }

    fun update(tempBan: TempBan): Unit = entityManagerFactory.withEntityManager {
        merge(tempBan)
    }

    fun update(guildId: Long, userId: Long, expiration: Instant? = null, reason: String? = null): Unit = entityManagerFactory.withEntityManager {
        val update = criteriaBuilder.update<TempBan>()
        val root = update.from<TempBan>()

        if (expiration != null) update.set(root[TempBan::expiresAt], expiration)
        if (reason != null) update.set(root[TempBan::reason], reason)

        update.where(root[TempBan::guildId] equal guildId, root[TempBan::userId] equal userId)

        createQuery(update).executeUpdate()
    }
}
