package com.molikuner.calk

import java.math.BigDecimal

private val HELP_MESSAGE = """
    ;`q`                    |  exit/return
    ;`-> z = 2`, `-> z<2>`  |  define variable z as 2
    ;`2 + 2`                |  calk 2 + 2
    ;`x<5>=x*2`             |  calk given times x time 2 where x is at beginning 5
    ;`x=2|y<1>:=y*x'`       |  print only y (expressed by `:`), x has no value at beginning
    ;                       |  use current calked x when calking y (expressed by `'`)
    ;`h`, `help`, `?`       |  this help message
    ;""".trimMargin(";")

private val VARIABLES = mutableMapOf(
        "PI" to BigDecimal.valueOf(Math.PI),
        "e" to BigDecimal.valueOf(Math.E)
)

private fun ask(question: String): String? {
    print("$question ")
    return readLine()?.trim()?.let { if (it != "q") it else null }
}

fun main() {
    while (true) {
        val line = ask("Give me something to calk:") ?: break
        if (line.matches("""h(elp)?|\?""".toRegex())) {
            println(HELP_MESSAGE)
            continue
        }
        try {
            if (line.startsWith("->")) {
                handleVariable(line)
            } else if ("=" !in line) {
                println(line.calculate(
                    replaces = VARIABLES
                ).stripTrailingZeros())
            } else {
                line.sequent(
                    times = ask("How often should it be calked?")?.toInt() ?: continue,
                    constants = VARIABLES
                ) {
                    return@sequent if ("<>" in this) {
                        it.copy(currentVal = ask("Which value should have '${it.valName}'?")?.toBigDecimal())
                    } else it
                }
            }
        } catch (e: Exception) {
            System.err.println("Error: ${e.localizedMessage ?: e.message}")
        }
    }
}

private fun handleVariable(input: String) {
    input.drop(2).trim().toValue().also {
        if (it.currentVal == null && it.formula == null) {
            VARIABLES.remove(it.valName)
        } else if (it.currentVal != null) {
            VARIABLES[it.valName] = it.currentVal
        } else {
            throw IllegalArgumentException("Can not set variable to non determinant value")
        }
    }
}
