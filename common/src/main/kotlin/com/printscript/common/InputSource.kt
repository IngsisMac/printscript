package com.printscript.common

fun interface InputSource {
    fun input(prompt: String): String
}
