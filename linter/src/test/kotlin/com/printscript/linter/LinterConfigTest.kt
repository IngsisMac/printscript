package com.printscript.linter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LinterConfigTest {
    private lateinit var emptyConfigMap: Map<String, Any?>
    private lateinit var fullConfigMap: Map<String, Any?>

    @BeforeEach
    fun setUp() {
        emptyConfigMap = emptyMap()
        fullConfigMap =
            mapOf(
                "identifier_format" to "camel case",
                "mandatory-variable-or-literal-in-println" to true,
                "mandatory-variable-or-literal-in-readInput" to true,
            )
    }

    @Test
    @DisplayName("Creación de configuración desde mapa vacío produce valores por defecto")
    fun emptyMapProducesDefaultConfig() {
        val config = LinterConfig.fromMap(emptyConfigMap)

        assertEquals(IdentifierFormat.NONE, config.identifierFormat)
        assertFalse(config.mandatoryVariableOrLiteralInPrintln)
        assertFalse(config.mandatoryVariableOrLiteralInReadInput)
    }

    @Test
    @DisplayName("Creación de configuración desde mapa con claves TCK parsea todos los atributos")
    fun fullMapParsesAllAttributesCorrectly() {
        val config = LinterConfig.fromMap(fullConfigMap)

        assertEquals(IdentifierFormat.CAMEL_CASE, config.identifierFormat)
        assertTrue(config.mandatoryVariableOrLiteralInPrintln)
        assertTrue(config.mandatoryVariableOrLiteralInReadInput)
    }

    @Test
    @DisplayName("Creación de configuración tolera valores booleanos expresados como cadenas")
    fun mapWithBooleanStringsParsesCorrectly() {
        val stringConfigMap =
            mapOf(
                "identifier_format" to "snake case",
                "mandatory-variable-or-literal-in-println" to "true",
                "mandatory-variable-or-literal-in-readInput" to "false",
            )

        val config = LinterConfig.fromMap(stringConfigMap)

        assertEquals(IdentifierFormat.SNAKE_CASE, config.identifierFormat)
        assertTrue(config.mandatoryVariableOrLiteralInPrintln)
        assertFalse(config.mandatoryVariableOrLiteralInReadInput)
    }
}
