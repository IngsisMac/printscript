package com.printscript.common

enum class Version(val identifier: String) {
    V1_0("1.0"),
    V1_1("1.1");

    companion object {
        fun from(id: String): Version = entries.find { it.identifier == id }
            ?: throw IllegalArgumentException("Unknown version: $id")
    }

    override fun toString() = identifier
}
