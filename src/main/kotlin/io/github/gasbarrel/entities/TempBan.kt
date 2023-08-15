package io.github.gasbarrel.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table
class TempBan(
    @Id
    @Column(nullable = false)
    val id: Int,
    @Column(nullable = false)
    val guildId: Long,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column(nullable = false)
    var reason: String
)

// https://www.baeldung.com/jpa-composite-primary-keys
//@Embeddable
//class TempBanId(
//    val guildId: Long,
//    val userId: Long,
//) : Serializable {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as TempBanId
//
//        if (guildId != other.guildId) return false
//        if (userId != other.userId) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = guildId.hashCode()
//        result = 31 * result + userId.hashCode()
//        return result
//    }
//}
