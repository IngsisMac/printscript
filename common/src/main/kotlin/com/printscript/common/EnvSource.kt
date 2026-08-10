package com.printscript.common

fun interface EnvSource {
    fun env(name: String): String?
}
