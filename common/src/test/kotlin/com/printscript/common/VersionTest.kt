package com.printscript.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VersionTest {
    private lateinit var v10Identifier: String
    private lateinit var v11Identifier: String

    @BeforeEach
    fun setUp() {
        v10Identifier = "1.0"
        v11Identifier = "1.1"
    }

    @Test
    @DisplayName("Version.from resuelve cadenas 1.0 y 1.1 a sus respectivos valores enum")
    fun versionFromResuelveCadenasValidasAVersionEnum() {
        val id10 = v10Identifier
        val id11 = v11Identifier

        val version10 = Version.from(id10)
        val version11 = Version.from(id11)

        assertEquals(Version.V1_0, version10)
        assertEquals(Version.V1_1, version11)
    }

    @Test
    @DisplayName("Version.from lanza IllegalArgumentException ante identificadores de versión no reconocidos")
    fun versionFromLanzaIllegalArgumentExceptionAnteIdentificadoresNoReconocidos() {
        val unknownId = "2.0"

        val exception =
            assertThrows<IllegalArgumentException> {
                Version.from(unknownId)
            }

        assertEquals("Unknown version: 2.0", exception.message)
    }

    @Test
    @DisplayName("Version toString retorna el identificador crudo de la versión")
    fun versionToStringRetornaElIdentificadorCrudoDeLaVersion() {
        val version10 = Version.V1_0
        val version11 = Version.V1_1

        assertEquals("1.0", version10.toString())
        assertEquals("1.1", version11.toString())
    }
}
