package com.molikuner.calk

import com.molikuner.bigMath.pow
import java.math.BigDecimal
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

fun BigDecimal.power(y: BigDecimal) = this.pow(y)
