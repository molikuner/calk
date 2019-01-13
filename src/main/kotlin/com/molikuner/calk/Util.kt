package com.molikuner.calk

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

inline fun <K, V, R> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
    return this.mapValues(transform).filterValues { it != null }.mapValues { it.value!! }
}
