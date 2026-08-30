package com.printscript.common

fun interface EnvSource {
    fun env(name: String): String?

    companion object {
        val DENY: EnvSource = EnvSource { null }
        val SYSTEM: EnvSource = EnvSource { System.getenv(it) }
    }
}
