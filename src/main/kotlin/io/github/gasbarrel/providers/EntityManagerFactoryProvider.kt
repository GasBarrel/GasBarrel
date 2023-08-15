package io.github.gasbarrel.providers

import com.freya02.botcommands.api.core.service.annotations.BService
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import javax.sql.DataSource

@Suppress("unused")
object EntityManagerFactoryProvider {
    @BService
    fun get(dataSource: DataSource): EntityManagerFactory {
        return Persistence.createEntityManagerFactory("io.github.gasbarrel", mapOf(
            // nonJtaDataSource: I don't think we're interested in distributed transactions
            "jakarta.persistence.nonJtaDataSource" to dataSource
        ));
    }
}

inline fun <R> EntityManagerFactory.withEntityManager(block: EntityManager.() -> R): R = createEntityManager().use { em ->
    em.transaction.begin()
    try {
        block(em).also {
            em.transaction.commit()
        }
    } catch (e: Exception) {
        em.transaction.rollback()
        throw e
    }
}
