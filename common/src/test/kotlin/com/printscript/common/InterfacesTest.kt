package com.printscript.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class InterfacesTest {
    private lateinit var printedMessages: MutableList<String>
    private lateinit var envVariables: Map<String, String>

    @BeforeEach
    fun setUp() {
        printedMessages = mutableListOf()
        envVariables = mapOf("PATH" to "/usr/bin", "ENV_VAR" to "test_value")
    }

    @Test
    @DisplayName("OutputEmitter ejecuta correctamente la emisión de mensajes")
    fun outputEmitterEjecutaCorrectamenteLaEmisionDeMensajes() {
        val emitter = OutputEmitter { message -> printedMessages.add(message) }

        emitter.print("Hello World")

        assertEquals(listOf("Hello World"), printedMessages)
    }

    @Test
    @DisplayName("InputSource ejecuta correctamente la solicitud de entrada")
    fun inputSourceEjecutaCorrectamenteLaSolicitudDeEntrada() {
        val inputSource = InputSource { prompt -> "Response for $prompt" }

        val response = inputSource.input("Enter name:")

        assertEquals("Response for Enter name:", response)
    }

    @Test
    @DisplayName("EnvSource ejecuta correctamente la consulta de variables de entorno")
    fun envSourceEjecutaCorrectamenteLaConsultaDeVariablesDeEntorno() {
        val envSource = EnvSource { name -> envVariables[name] }

        val existingVal = envSource.env("ENV_VAR")
        val missingVal = envSource.env("NON_EXISTENT")

        assertEquals("test_value", existingVal)
        assertNull(missingVal)
    }
}
