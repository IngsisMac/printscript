package com.printscript.linter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class IdentifierFormatTest {
    private lateinit var camelCaseFormat: IdentifierFormat
    private lateinit var snakeCaseFormat: IdentifierFormat
    private lateinit var noneFormat: IdentifierFormat

    @BeforeEach
    fun setUp() {
        camelCaseFormat = IdentifierFormat.CAMEL_CASE
        snakeCaseFormat = IdentifierFormat.SNAKE_CASE
        noneFormat = IdentifierFormat.NONE
    }

    @Test
    @DisplayName("Identificador camelCase válido pasa la validación")
    fun validCamelCaseIdentifierMatches() {
        assertTrue(camelCaseFormat.matches("myVariable"))
        assertTrue(camelCaseFormat.matches("userName1"))
        assertTrue(camelCaseFormat.matches("x"))
    }

    @Test
    @DisplayName("Identificador con guion bajo falla en validación camelCase")
    fun snakeCaseIdentifierFailsCamelCase() {
        assertFalse(camelCaseFormat.matches("my_variable"))
    }

    @Test
    @DisplayName("Identificador con mayúscula inicial falla en validación camelCase")
    fun pascalCaseIdentifierFailsCamelCase() {
        assertFalse(camelCaseFormat.matches("MyVariable"))
    }

    @Test
    @DisplayName("Identificador snake_case válido pasa la validación")
    fun validSnakeCaseIdentifierMatches() {
        assertTrue(snakeCaseFormat.matches("my_variable"))
        assertTrue(snakeCaseFormat.matches("user_name_1"))
        assertTrue(snakeCaseFormat.matches("x"))
    }

    @Test
    @DisplayName("Identificador camelCase con mayúsculas falla en validación snake_case")
    fun camelCaseIdentifierFailsSnakeCase() {
        assertFalse(snakeCaseFormat.matches("myVariable"))
    }

    @Test
    @DisplayName("Formato NONE siempre valida cualquier identificador")
    fun noneFormatMatchesAnyIdentifier() {
        assertTrue(noneFormat.matches("myVariable"))
        assertTrue(noneFormat.matches("my_variable"))
        assertTrue(noneFormat.matches("MyVariable"))
    }

    @Test
    @DisplayName("Parseo de formato desde cadenas de texto con varias convenciones")
    fun fromStringParsesFormatsCorrectly() {
        assertEquals(IdentifierFormat.CAMEL_CASE, IdentifierFormat.fromString("camel case"))
        assertEquals(IdentifierFormat.CAMEL_CASE, IdentifierFormat.fromString("camelCase"))
        assertEquals(IdentifierFormat.CAMEL_CASE, IdentifierFormat.fromString("camel_case"))
        assertEquals(IdentifierFormat.SNAKE_CASE, IdentifierFormat.fromString("snake case"))
        assertEquals(IdentifierFormat.SNAKE_CASE, IdentifierFormat.fromString("snakeCase"))
        assertEquals(IdentifierFormat.SNAKE_CASE, IdentifierFormat.fromString("snake_case"))
        assertEquals(IdentifierFormat.NONE, IdentifierFormat.fromString(null))
        assertEquals(IdentifierFormat.NONE, IdentifierFormat.fromString(""))
        assertEquals(IdentifierFormat.NONE, IdentifierFormat.fromString("invalid"))
    }
}
