package com.molikuner.calk

import java.math.BigDecimal

data class Value(
    val currentVal: BigDecimal?,
    val formula: String?,
    val print: Boolean,
    val valName: String
) {
    companion object {
        internal val FORMAT =
            """^[a-z]+\s*(?:<\s*([0-9]+(?:\.[0-9]+)?)?\s*>)?\s*:?(?:\s*=\s*(?:([0-9]+(?:\.[0-9]+)?)|.*))?$"""
                .toRegex(RegexOption.IGNORE_CASE)
    }
}

// x<5> is equal to x=5
fun String.toValue(default: BigDecimal? = null, overrideResult: String.(Value) -> Value = { it }): Value {
    val match = Value.FORMAT.matchEntire(this)?.groupValues
    if (match == null || ("<" !in this && "=" !in this))
        throw IllegalArgumentException("Can not parse '$this' as a value")

    return Value(
            currentVal = match.let { list ->
                val definedValue = list[1].takeIf { it.isNotEmpty() } ?: list[2].takeIf { it.isNotEmpty() }
                return@let definedValue?.let { BigDecimal(it.trim()) }
            } ?: default,
        formula = this.split("=").getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() },
            print = this.contains(':'),
            valName = this.split("""[:<=]""".toRegex())[0].trim()
    ).let {
        return@let this.overrideResult(it)
    }
}

fun String.sequent(
    times: Int,
    constants: Map<String, BigDecimal>,
    askForValue: String.(Value) -> Value
) {
    // setup
    var values = this
        .split('|')
        .map { it.trim().toValue(constants["DEFAULT"], askForValue) }
        .groupBy { it.valName }
        .mapValues { entry ->
        if (entry.value.size != 1) throw IllegalArgumentException("multiple definitions found for: '${entry.key}'")
        return@mapValues entry.value[0]
    }
    val printAnyways = ':' !in this

    // sequent
    values.printKeys(printAnyways)
    values.printValues(0, printAnyways)
    for (i in 1 until times + 1) {
        // calculate new
        val calked = mutableMapOf<String, Value>()
        values.forEach { key, value ->
            calked["$key'"] = value.copy(
                    currentVal = value.formula?.calculate(
                            replaces = (values + calked).mapValuesNotNull { e -> e.value.currentVal } + constants
                    )
            )
        }

        // save and print
        values = calked.mapKeys { it.key.dropLast(1) }
        values.printValues(i, printAnyways)
    }
}

fun Map<String, Value>.printKeys(printAnyways: Boolean) {
    this.print("-") {
        return@print if (printAnyways || it.value.print) {
            it.key
        } else {
            null
        }
    }
}

fun Map<String, Value>.printValues(num: Int, printAnyways: Boolean) {
    this.print(num.toString()) {
        return@print if (printAnyways || it.value.print) {
            it.value.currentVal?.stripTrailingZeros()?.toString() ?: "-"
        } else {
            null
        }
    }
}

inline fun Map<String, Value>.print(num: String, transform: (Map.Entry<String, Value>) -> String?) {
    println("$num: " + this.mapNotNull(transform).joinToString(" | "))
}
