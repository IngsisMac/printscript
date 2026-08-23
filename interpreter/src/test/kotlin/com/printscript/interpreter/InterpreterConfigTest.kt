package com.printscript.interpreter

import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class InterpreterConfigTest {
    private lateinit var version10: Version
    private lateinit var version11: Version

    @BeforeEach
    fun setUp() {
        version10 = Version.V1_0
        version11 = Version.V1_1
    }

    @Test
    @DisplayName("La configuración de la versión 1.0 se instancia correctamente")
    fun configuracionVersion10SeInstanciaCorrectamente() {
        val config = InterpreterConfig.from(version10)

        assertNotNull(config)
        assertEquals(version10, config.version)
        assertEquals(3, config.statementEvaluators.size)
        assertEquals(4, config.expressionEvaluators.size)
    }

    @Test
    @DisplayName("La configuración de la versión 1.1 se instancia con evaluadores extendidos")
    fun configuracionVersion11SeInstanciaConEvaluadoresExtendidos() {
        val config = InterpreterConfig.from(version11)

        assertNotNull(config)
        assertEquals(version11, config.version)
        assertEquals(4, config.statementEvaluators.size)
        assertEquals(6, config.expressionEvaluators.size)
    }
}
