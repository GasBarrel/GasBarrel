package io.github.gasbarrel.repositories

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.*
import org.intellij.lang.annotations.Language
import kotlin.reflect.KProperty1

@Suppress("SqlSourceToSinkFlow")
inline fun <reified T : Any> EntityManager.query(
    @Language("JPAQL") qlString: String,
    block: TypedQuery<T>.() -> Unit
): TypedQuery<T> =
    createQuery(qlString, T::class.java).apply(block)

inline fun <reified T : Any> EntityManager.namedQuery(name: String, block: TypedQuery<T>.() -> Unit): TypedQuery<T> =
    createNamedQuery(name, T::class.java).apply(block)

inline fun <reified T : Any> EntityManager.find(primaryKey: Any): T? = find(T::class.java, primaryKey)

inline fun <reified T : Any> CriteriaBuilder.construct(vararg selections: Selection<*>): CompoundSelection<T> =
    construct(T::class.java, *selections)

inline fun <reified T : Any> CriteriaBuilder.update() : CriteriaUpdate<T> = createCriteriaUpdate(T::class.java)

inline fun <reified T : Any> CriteriaBuilder.query(): CriteriaQuery<T> = createQuery(T::class.java)
inline fun <reified T : Any> CommonAbstractCriteria.subquery(): Subquery<T> = subquery(T::class.java)
inline fun <reified T : Any> AbstractQuery<*>.from(): Root<T> = from(T::class.java)
inline fun <reified T : Any> CriteriaUpdate<T>.from(): Root<T> = from(T::class.java)

fun <S : Any, T : Any> From<*, S>.join(prop: KProperty1<S, T>): Join<S, T> = join(prop.name)
operator fun <S : Any, T : Any> Path<S>.get(prop: KProperty1<S, T>): Path<T> = get(prop.name)

context(EntityManager)
infix fun Predicate.and(predicate: Predicate): Predicate = criteriaBuilder.and(this, predicate)

context(EntityManager)
infix fun Predicate.or(predicate: Predicate): Predicate = criteriaBuilder.or(this, predicate)

context(EntityManager)
infix fun <T : Any> Expression<T>.equal(o: T?): Predicate = criteriaBuilder.equal(this, o)

context(EntityManager)
infix fun <T : Any> Expression<T>.equal(o: Expression<T>): Predicate = criteriaBuilder.equal(this, o)

context(EntityManager)
infix fun <T : Any> Expression<T>.notEqual(o: T?): Predicate = criteriaBuilder.notEqual(this, o)

context(EntityManager)
infix fun <T : Any> Expression<T>.notEqual(o: Expression<T>): Predicate = criteriaBuilder.notEqual(this, o)

infix fun Expression<*>.`in`(expression: Expression<*>): Predicate = this.`in`(expression)
