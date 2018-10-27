package com.molikuner.calk

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

inline fun <reified R : Any> String.forAny(s: Char, action: (index: Int) -> R): R? = this.forEachIndexed { index, c ->
    if (c == s) {
        try {
            return action(index)
        } catch (ignore: IllegalArgumentException) {
        }
    }
}.let { null }

inline fun <R> simpleTry(
    exceptionType: KClass<out Exception> = IllegalArgumentException::class,
    crossinline action: () -> R
) = try {
    action()
} catch (e: Exception) {
    if (!exceptionType.isInstance(e)) throw e else null
}

fun BigInteger.factorial(): BigInteger {
    fun BigInteger.dec() =
        if (this > BigInteger.ZERO) this - BigInteger.ONE else throw IllegalArgumentException("can't calculate faculty of negative numbers")

    return if (this == BigInteger.ONE || this == BigInteger.ZERO) this else this.dec().factorial() * this
}

fun BigDecimal.factorialExact(): BigDecimal = this.toBigIntegerExact().factorial().toBigDecimal()
