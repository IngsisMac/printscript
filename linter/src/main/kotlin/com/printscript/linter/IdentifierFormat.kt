package com.printscript.linter

enum class IdentifierFormat {
    CAMEL_CASE,
    SNAKE_CASE,
    NONE;

    fun matches(identifier: String): Boolean =
        when (this) {
            CAMEL_CASE -> CAMEL_CASE_REGEX.matches(identifier)
            SNAKE_CASE -> SNAKE_CASE_REGEX.matches(identifier)
            NONE -> true
        }

    companion object {
        private val CAMEL_CASE_REGEX = Regex("^[a-z][a-zA-Z0-9]*$")
        private val SNAKE_CASE_REGEX = Regex("^[a-z][a-z0-9_]*$")

        fun fromString(raw: String?): IdentifierFormat {
            if (raw.isNullOrBlank()) return NONE
            val normalized = raw.trim().lowercase()
            return when {
                normalized == "camel case" || normalized == "camelcase" || normalized == "camel_case" -> CAMEL_CASE
                normalized == "snake case" || normalized == "snakecase" || normalized == "snake_case" -> SNAKE_CASE
                else -> NONE
            }
        }
    }
}
