package com.printscript.formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FormatterConfigTest {

    private lateinit var defaultConfig: FormatterConfig

    @BeforeEach
    fun setUp() {
        defaultConfig = FormatterConfig()
    }

    @Test
    @DisplayName("Configuración por defecto tiene los valores esperados")
    fun configuracionPorDefectoTieneValoresEsperados() {
        assertTrue(defaultConfig.enforceSpacingAroundEquals)
        assertFalse(defaultConfig.enforceNoSpacingAroundEquals)
        assertFalse(defaultConfig.enforceSpacingBeforeColonInDeclaration)
        assertTrue(defaultConfig.enforceSpacingAfterColonInDeclaration)
        assertTrue(defaultConfig.mandatorySingleSpaceSeparation)
        assertTrue(defaultConfig.mandatorySpaceSurroundingOperations)
        assertTrue(defaultConfig.mandatoryLineBreakAfterStatement)
        assertEquals(1, defaultConfig.lineBreaksAfterPrintln)
        assertTrue(defaultConfig.ifBraceSameLine)
        assertFalse(defaultConfig.ifBraceBelowLine)
        assertEquals(4, defaultConfig.indentInsideIf)
    }

    @Test
    @DisplayName("Parseo desde mapa vacío devuelve la configuración por defecto")
    fun parseoDesdeMapaVacioDevuelveConfiguracionPorDefecto() {
        val map = emptyMap<String, Any?>()

        val config = FormatterConfig.fromMap(map)

        assertEquals(defaultConfig, config)
    }

    @Test
    @DisplayName("Parseo desde mapa con claves personalizadas y tipos booleanos y numéricos")
    fun parseoDesdeMapaConClavesPersonalizadas() {
        val map = mapOf(
            "enforce-spacing-around-equals" to false,
            "enforce-spacing-before-colon-in-declaration" to true,
            "enforce-spacing-after-colon-in-declaration" to false,
            "line-breaks-after-println" to 2,
            "indent-inside-if" to 2,
        )

        val config = FormatterConfig.fromMap(map)

        assertFalse(config.enforceSpacingAroundEquals)
        assertTrue(config.enforceSpacingBeforeColonInDeclaration)
        assertFalse(config.enforceSpacingAfterColonInDeclaration)
        assertEquals(2, config.lineBreaksAfterPrintln)
        assertEquals(2, config.indentInsideIf)
    }

    @Test
    @DisplayName("Parseo desde mapa con valores representados como String")
    fun parseoDesdeMapaConValoresString() {
        val map = mapOf(
            "enforce-spacing-around-equals" to "false",
            "enforce-spacing-before-colon-in-declaration" to "true",
            "line-breaks-after-println" to "3",
            "indent-inside-if" to "8",
        )

        val config = FormatterConfig.fromMap(map)

        assertFalse(config.enforceSpacingAroundEquals)
        assertTrue(config.enforceSpacingBeforeColonInDeclaration)
        assertEquals(3, config.lineBreaksAfterPrintln)
        assertEquals(8, config.indentInsideIf)
    }

    @Test
    @DisplayName("Clave enforce-no-spacing-around-equals sobrescribe enforce-spacing-around-equals")
    fun claveEnforceNoSpacingSobrescribeEnforceSpacing() {
        val map = mapOf(
            "enforce-spacing-around-equals" to true,
            "enforce-no-spacing-around-equals" to true,
        )

        val config = FormatterConfig.fromMap(map)

        assertFalse(config.enforceSpacingAroundEquals)
        assertTrue(config.enforceNoSpacingAroundEquals)
    }

    @Test
    @DisplayName("Clave if-brace-below-line sobrescribe if-brace-same-line")
    fun claveIfBraceBelowLineSobrescribeIfBraceSameLine() {
        val map = mapOf(
            "if-brace-same-line" to true,
            "if-brace-below-line" to true,
        )

        val config = FormatterConfig.fromMap(map)

        assertFalse(config.ifBraceSameLine)
        assertTrue(config.ifBraceBelowLine)
    }

    @Test
    @DisplayName("Manejo de valores inválidos en mapa utiliza valores por defecto")
    fun manejoDeValoresInvalidosUtilizaValoresPorDefecto() {
        val map = mapOf<String, Any?>(
            "enforce-spacing-around-equals" to 123,
            "line-breaks-after-println" to "invalid-number",
        )

        val config = FormatterConfig.fromMap(map)

        assertTrue(config.enforceSpacingAroundEquals)
        assertEquals(1, config.lineBreaksAfterPrintln)
    }
}
