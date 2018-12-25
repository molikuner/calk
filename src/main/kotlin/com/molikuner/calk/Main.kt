package com.molikuner.calk

import java.math.BigDecimal

val constants = mapOf(
        "PI" to BigDecimal.valueOf(Math.PI),
        "e" to BigDecimal.valueOf(Math.E)
)

private fun ask(question: String): String? {
    print("$question ")
    return readLine()?.trim()?.let { if (it != "q") it else null }
}

fun main() {
    var default: BigDecimal? = null
    while (true) {
        val line = ask("Give me something to calk:") ?: break
        try {
            if (line.startsWith("->")) {
                default = line.drop(2).trim().toValue().currentVal ?: {
                    println("Could not define the default value")
                    null
                }() ?: continue
            } else if ("=" !in line) {
                println(line.calculate(
                        replaces = default?.let { return@let constants + mapOf("default" to it) } ?: constants
                ).stripTrailingZeros())
            } else {
                line.sequent(
                        times = ask("How often should it be calked?")?.toInt() ?: break,
                        default = default,
                        constants = constants
                )
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.localizedMessage ?: e.message}")
        }
    }
}
