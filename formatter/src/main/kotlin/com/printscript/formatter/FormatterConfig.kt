package com.printscript.formatter

private const val DEFAULT_INDENT_INSIDE_IF = 2

data class FormatterConfig(
    val enforceSpacingAroundEquals: Boolean? = null,
    val enforceNoSpacingAroundEquals: Boolean = false,
    val enforceSpacingBeforeColonInDeclaration: Boolean? = null,
    val enforceSpacingAfterColonInDeclaration: Boolean? = null,
    val mandatorySingleSpaceSeparation: Boolean = false,
    val mandatorySpaceSurroundingOperations: Boolean = true,
    val mandatoryLineBreakAfterStatement: Boolean = true,
    val lineBreaksAfterPrintln: Int = 0,
    val ifBraceSameLine: Boolean = true,
    val ifBraceBelowLine: Boolean = false,
    val indentInsideIf: Int = DEFAULT_INDENT_INSIDE_IF,
    val enforceNoSpaceBeforeSemicolon: Boolean = true,
    val maxBlankLinesBetweenStatements: Int = 1,
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): FormatterConfig {
            val base = parseSpacingConfig(map)
            return parseStructureConfig(map, base)
        }

        private fun parseSpacingConfig(map: Map<String, Any?>): FormatterConfig =
            FormatterConfig(
                enforceSpacingAroundEquals = parseEquals(map),
                enforceNoSpacingAroundEquals = getBool(map, "enforce-no-spacing-around-equals", false),
                enforceSpacingBeforeColonInDeclaration =
                    if ("enforce-spacing-before-colon-in-declaration" in map) {
                        getBool(map, "enforce-spacing-before-colon-in-declaration", false)
                    } else {
                        null
                    },
                enforceSpacingAfterColonInDeclaration =
                    if ("enforce-spacing-after-colon-in-declaration" in map) {
                        getBool(map, "enforce-spacing-after-colon-in-declaration", true)
                    } else {
                        null
                    },
                mandatorySingleSpaceSeparation = getBool(map, getSingleSpaceKey(map), false),
                mandatorySpaceSurroundingOperations = getBool(map, "mandatory-space-surrounding-operations", true),
                enforceNoSpaceBeforeSemicolon = getBool(map, "enforce-no-space-before-semicolon", true),
            )

        private fun parseStructureConfig(
            map: Map<String, Any?>,
            base: FormatterConfig,
        ): FormatterConfig =
            base.copy(
                mandatoryLineBreakAfterStatement = getBool(map, "mandatory-line-break-after-statement", true),
                lineBreaksAfterPrintln = getInt(map, "line-breaks-after-println", 0),
                ifBraceSameLine = parseBraceSameLine(map),
                ifBraceBelowLine = getBool(map, "if-brace-below-line", false),
                indentInsideIf = getInt(map, "indent-inside-if", DEFAULT_INDENT_INSIDE_IF),
                maxBlankLinesBetweenStatements = getInt(map, "max-blank-lines-between-statements", 1),
            )

        private fun parseEquals(map: Map<String, Any?>): Boolean? {
            if ("enforce-no-spacing-around-equals" in map && getBool(map, "enforce-no-spacing-around-equals", false)) {
                return false
            }
            if ("enforce-spacing-around-equals" in map) {
                return getBool(map, "enforce-spacing-around-equals", true)
            }
            return null
        }

        private fun parseBraceSameLine(map: Map<String, Any?>): Boolean {
            val below = getBool(map, "if-brace-below-line", false)
            return if (below) false else getBool(map, "if-brace-same-line", true)
        }

        private fun getSingleSpaceKey(map: Map<String, Any?>): String =
            if ("enforce-single-space-separation" in map) {
                "enforce-single-space-separation"
            } else {
                "mandatory-single-space-separation"
            }

        private fun getBool(
            map: Map<String, Any?>,
            key: String,
            default: Boolean,
        ): Boolean {
            val value = map[key] ?: return default
            return (value as? Boolean) ?: (value as? String)?.toBoolean() ?: default
        }

        private fun getInt(
            map: Map<String, Any?>,
            key: String,
            default: Int,
        ): Int {
            val value = map[key] ?: return default
            return (value as? Number)?.toInt() ?: (value as? String)?.toIntOrNull() ?: default
        }
    }
}
