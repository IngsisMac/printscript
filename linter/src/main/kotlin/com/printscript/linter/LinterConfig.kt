package com.printscript.linter

data class LinterConfig(
    val identifierFormat: IdentifierFormat = IdentifierFormat.NONE,
    val mandatoryVariableOrLiteralInPrintln: Boolean = false,
    val mandatoryVariableOrLiteralInReadInput: Boolean = false,
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): LinterConfig {
            fun getBool(
                key: String,
                default: Boolean = false,
            ): Boolean {
                val value = map[key] ?: return default
                return when (value) {
                    is Boolean -> value
                    is String -> value.toBoolean()
                    else -> default
                }
            }

            val formatRaw = map["identifier_format"]?.toString()
            val identifierFormat = IdentifierFormat.fromString(formatRaw)

            return LinterConfig(
                identifierFormat = identifierFormat,
                mandatoryVariableOrLiteralInPrintln = getBool("mandatory-variable-or-literal-in-println"),
                mandatoryVariableOrLiteralInReadInput = getBool("mandatory-variable-or-literal-in-readInput"),
            )
        }
    }
}
