package com.molikuner.calk

fun main() {
    do {
        print("Give me sth. to calk: ")
        val l = readLine()
        try {
            l?.let { if (it != "e") println(it.calculate()) }
        } catch (e: Exception) {
            System.err.println("Error: ${e.localizedMessage}")
        }
    } while (l != null && l != "e")
}
