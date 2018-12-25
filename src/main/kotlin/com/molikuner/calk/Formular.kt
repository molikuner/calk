package com.molikuner.calk

import com.molikuner.bigMath.factorial
import java.math.BigDecimal

internal val numberRegex = """^[0-9]+(?:\.[0-9]*)?$""".toRegex()
private val patternRegex = """^[-+]*\s*(?:[0-9.a-zA-Z]+'?|\(.+\)|)\s*!?$""".toRegex()
private val replacementRegex = """^[a-z]+'?$""".toRegex(RegexOption.IGNORE_CASE)
private val bracketRegex = """^\(.+\)$""".toRegex()

private inline fun String.parse(
    c: Char,
    arg1: String.(replaces: Map<String, BigDecimal>) -> BigDecimal,
    arg2: String.(replaces: Map<String, BigDecimal>) -> BigDecimal,
    replaces: Map<String, BigDecimal>,
    action: BigDecimal.(i: BigDecimal) -> BigDecimal
) = this.forAny(c) {
    this.substring(0, it).trim().arg1(replaces).action(this.substring(it + 1).trim().arg2(replaces))
} ?: throw IllegalArgumentException("'$this' can't be parsed ($c)")

private fun String.expression(replaces: Map<String, BigDecimal>): BigDecimal {
    fun String.on(c: Char, action: BigDecimal.(i: BigDecimal) -> BigDecimal) =
        if (this.contains(c)) simpleTry { this.parse(c, String::term, String::expression, replaces, action) } else null

    this.on('+', BigDecimal::plus)?.also { return it }
    this.on('-', BigDecimal::minus)?.also { return it }
    return this.trim().term(replaces)
}

private fun String.term(replaces: Map<String, BigDecimal>): BigDecimal {
    fun String.on(c: Char, action: BigDecimal.(i: BigDecimal) -> BigDecimal) =
        if (this.contains(c)) simpleTry { this.parse(c, String::factor, String::term, replaces, action) } else null

    this.on('*', BigDecimal::multiply)?.also { return it }
    this.on('/', BigDecimal::divide)?.also { return it }
    this.on('^', BigDecimal::power)?.also { return it }
    this.on('%', BigDecimal::remainder)?.also { return it }

    return this.trim().factor(replaces)
}

private fun String.factor(replaces: Map<String, BigDecimal>): BigDecimal {
    if (!this.matches(patternRegex)) throw IllegalArgumentException("malformed pattern in: '$this'?")
    return when {
        this.matches(bracketRegex) -> this.substring(1, this.length - 1).trim().expression(replaces)
        this.startsWith('-') -> this.substring(1).trim().factor(replaces).negate()
        this.startsWith('+') -> this.substring(1).trim().factor(replaces)
        this.endsWith('!') -> this.substring(0, this.length - 1).trim().factor(replaces).factorial().toBigDecimal()
        else -> this.trim().number(replaces)
    }
}

private fun String.number(replaces: Map<String, BigDecimal>): BigDecimal {
    if (this.matches(numberRegex)) return BigDecimal(this)
    if (!this.matches(replacementRegex) || this !in replaces)
        throw IllegalArgumentException("can't create number of '$this'")
    return replaces.getValue(this)
}

fun String.calculate(replaces: Map<String, BigDecimal>) = this.expression(replaces)
