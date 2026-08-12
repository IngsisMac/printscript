package com.printscript.formatter

data class FormatterConfig(
    val enforceSpacingAroundEquals: Boolean = true,
    val enforceNoSpacingAroundEquals: Boolean = false,
    val enforceSpacingBeforeColonInDeclaration: Boolean = false,
    val enforceSpacingAfterColonInDeclaration: Boolean = true,
    val mandatorySingleSpaceSeparation: Boolean = true,
    val mandatorySpaceSurroundingOperations: Boolean = true,
    val mandatoryLineBreakAfterStatement: Boolean = true,
    val lineBreaksAfterPrintln: Int = 1,
    val ifBraceSameLine: Boolean = true,
    val ifBraceBelowLine: Boolean = false,
    val indentInsideIf: Int = 4,
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): FormatterConfig {
            fun getBool(
                key: String,
                default: Boolean,
            ): Boolean {
                val value = map[key] ?: return default
                return when (value) {
                    is Boolean -> value
                    is String -> value.toBoolean()
                    else -> default
                }
            }

            fun getInt(
                key: String,
                default: Int,
            ): Int {
                val value = map[key] ?: return default
                return when (value) {
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull() ?: default
                    else -> default
                }
            }

            val enforceEquals = getBool("enforce-spacing-around-equals", true)
            val enforceNoEquals = getBool("enforce-no-spacing-around-equals", false)

            val braceSameLine = getBool("if-brace-same-line", true)
            val braceBelowLine = getBool("if-brace-below-line", false)

            return FormatterConfig(
                enforceSpacingAroundEquals = if (enforceNoEquals) false else enforceEquals,
                enforceNoSpacingAroundEquals = enforceNoEquals,
                enforceSpacingBeforeColonInDeclaration = getBool("enforce-spacing-before-colon-in-declaration", false),
                enforceSpacingAfterColonInDeclaration = getBool("enforce-spacing-after-colon-in-declaration", true),
                mandatorySingleSpaceSeparation = getBool("mandatory-single-space-separation", true),
                mandatorySpaceSurroundingOperations = getBool("mandatory-space-surrounding-operations", true),
                mandatoryLineBreakAfterStatement = getBool("mandatory-line-break-after-statement", true),
                lineBreaksAfterPrintln = getInt("line-breaks-after-println", 1),
                ifBraceSameLine = if (braceBelowLine) false else braceSameLine,
                ifBraceBelowLine = braceBelowLine,
                indentInsideIf = getInt("indent-inside-if", 4),
            )
        }
    }
}
