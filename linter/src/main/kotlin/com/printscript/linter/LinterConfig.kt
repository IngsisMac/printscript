package com.printscript.linter

data class LinterConfig(
    val identifierFormat: IdentifierFormat = IdentifierFormat.NONE,
    val mandatoryVariableOrLiteralInPrintln: Boolean = false,
    val mandatoryVariableOrLiteralInReadInput: Boolean = false,
    val noUnusedVariables: Boolean = false,
    val noEmptyPrintln: Boolean = false,
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): LinterConfig {
            val formatRaw = map["identifier_format"]?.toString()
            return LinterConfig(
                identifierFormat = IdentifierFormat.fromString(formatRaw),
                mandatoryVariableOrLiteralInPrintln = getBool(map, "mandatory-variable-or-literal-in-println"),
                mandatoryVariableOrLiteralInReadInput = getBool(map, "mandatory-variable-or-literal-in-readInput"),
                noUnusedVariables = getBool(map, "no-unused-variables"),
                noEmptyPrintln = getBool(map, "no-empty-println"),
            )
        }

        private fun getBool(
            map: Map<String, Any?>,
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
    }
}
