package com.printscript.cli

import com.printscript.cli.util.ConfigLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ConfigLoaderTest {
    private lateinit var configLoader: ConfigLoader

    @BeforeEach
    fun setUp() {
        configLoader = ConfigLoader
    }

    @Test
    @DisplayName("Retorna mapa vacío cuando el archivo es nulo o no existe")
    fun retornaMapaVacioConArchivoNuloOInexistente(
        @TempDir tempDir: File,
    ) {
        val nullMap = configLoader.loadConfig(null)
        val missingFile = File(tempDir, "non_existent_config.json")
        val missingMap = configLoader.loadConfig(missingFile)

        assertTrue(nullMap.isEmpty())
        assertTrue(missingMap.isEmpty())
    }

    @Test
    @DisplayName("Retorna mapa vacío cuando el archivo de configuración está vacío")
    fun retornaMapaVacioConArchivoVacio(
        @TempDir tempDir: File,
    ) {
        val emptyFile = File(tempDir, "empty.json").apply { writeText("") }

        val emptyMap = configLoader.loadConfig(emptyFile)

        assertTrue(emptyMap.isEmpty())
    }

    @Test
    @DisplayName("Carga correctamente las configuraciones JSON con cadenas y valores numéricos")
    fun cargaDeConfiguracionDesdeArchivoJsonConTiposBasicos(
        @TempDir tempDir: File,
    ) {
        val configFile =
            File(tempDir, "valid_config.json").apply {
                writeText("{\"single_quote\": 'val', \"double_quote\": \"val2\", \"float_val\": 3.14}")
            }

        val map = configLoader.loadConfig(configFile)

        assertEquals("val", map["single_quote"])
        assertEquals("val2", map["double_quote"])
        assertEquals(3.14, map["float_val"])
    }

    @Test
    @DisplayName("Carga correctamente las configuraciones JSON con booleanos, nulos y entradas sin formato")
    fun cargaDeConfiguracionDesdeArchivoJsonConTiposAvanzados(
        @TempDir tempDir: File,
    ) {
        val configFile =
            File(tempDir, "valid_config_2.json").apply {
                writeText("{\"bool_true\": true, \"bool_false\": false, \"null_val\": null, \"raw_val\": unquoted}")
            }

        val map = configLoader.loadConfig(configFile)

        assertEquals(true, map["bool_true"])
        assertEquals(false, map["bool_false"])
        assertNull(map["null_val"])
        assertEquals("unquoted", map["raw_val"])
    }
}
