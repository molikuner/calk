package com.molikuner.calk

import java.math.BigDecimal

private val patternRegex = """^[-+]*\s*(?:[0-9.]+|\(.+\)|)\s*!?$""".toRegex()
private val numberRegex = """^[0-9]+(?:\.[0-9]*)?$""".toRegex()
private val bracketRegex = """^\(.+\)$""".toRegex()

private inline fun String.parse(
    c: Char,
    arg1: String.() -> BigDecimal,
    arg2: String.() -> BigDecimal,
    action: BigDecimal.(i: BigDecimal) -> BigDecimal
) = this.forAny(c) {
    this.substring(0, it).trim().arg1().action(this.substring(it + 1).trim().arg2())
} ?: throw IllegalArgumentException("'$this' can't be parsed ($c)")

private fun String.expression(): BigDecimal {
    fun String.parse(c: Char, action: BigDecimal.(i: BigDecimal) -> BigDecimal) =
        this.parse(c, String::term, String::expression, action)

    if (this.contains('+')) simpleTry { this.parse('+', BigDecimal::plus) }?.let { return it }
    if (this.contains('-')) simpleTry { this.parse('-', BigDecimal::minus) }?.let { return it }
    return this.term()
}

private fun String.term(): BigDecimal {
    fun String.parse(c: Char, action: BigDecimal.(i: BigDecimal) -> BigDecimal) =
        this.parse(c, String::factor, String::term, action)

    return when {
        this.contains('*') -> this.parse('*', BigDecimal::multiply)
        this.contains('/') -> this.parse('/', BigDecimal::divide)
        this.contains('^') -> TODO("^ is in the BigMath lib")
        this.contains('%') -> this.parse('%', BigDecimal::remainder)
        else -> this.trim().factor()
    }
}

private fun String.factor(): BigDecimal {
    if (!this.matches(patternRegex)) throw IllegalArgumentException("malformed pattern in: '$this'")
    return when {
        this.matches(bracketRegex) -> this.substring(1, this.length - 1).trim().expression()
        this.startsWith('-') -> this.substring(1).trim().factor().negate()
        this.startsWith('+') -> this.substring(1).trim().factor()
        this.endsWith('!') -> this.substring(0, this.length - 1).trim().factor().factorialExact()
        else -> this.trim().number()
    }
}

private fun String.number(): BigDecimal {
    if (!this.matches(numberRegex))
        throw IllegalArgumentException("can't create number of '$this'")
    return BigDecimal(this)
}

fun String.calculate() = this.expression()
