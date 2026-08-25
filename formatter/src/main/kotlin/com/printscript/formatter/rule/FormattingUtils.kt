package com.printscript.formatter.rule

import com.printscript.formatter.FormatterConfig

object FormattingUtils {
    fun getColonSpacing(
        enforceConfig: Boolean?,
        astSpace: Boolean?,
        defaultSpace: Boolean,
        config: FormatterConfig,
    ): String {
        if (config.mandatorySingleSpaceSeparation) return " "
        val space = enforceConfig ?: astSpace ?: defaultSpace
        return if (space) " " else ""
    }

    fun getEqualsSpacing(
        enforceConfig: Boolean?,
        astSpace: Boolean?,
        config: FormatterConfig,
    ): String {
        if (config.mandatorySingleSpaceSeparation) return " = "
        val space = enforceConfig ?: astSpace ?: true
        return if (space) " = " else "="
    }

    fun getSemicolon(config: FormatterConfig): String = if (config.enforceNoSpaceBeforeSemicolon) ";" else " ;"
}
