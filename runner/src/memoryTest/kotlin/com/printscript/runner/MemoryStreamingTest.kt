package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class MemoryStreamingTest {
    private lateinit var input: InputSource
    private lateinit var version: Version

    @BeforeEach
    fun setUp() {
        input = InputSource { "" }
        version = Version.V1_0
    }

    class PrintCounter(
        private val expectedMessage: String,
    ) : OutputEmitter {
        var count = 0
            private set

        override fun print(message: String) {
            if (message == expectedMessage) {
                count++
            } else {
                throw IllegalArgumentException("Unexpected message: $message")
            }
        }
    }

    class PrintCollector : OutputEmitter {
        val messages = ArrayList<String>()

        override fun print(message: String) {
            messages.add(message)
        }
    }

    @Test
    @DisplayName("Ejecución streaming procesa todas las líneas sin errores usando PrintCounter")
    fun executionCompletesSuccessfullyWithPrintCounterAndNoErrors() {
        val stream = MockInputStream()
        val counter = PrintCounter(MockInputStream.MESSAGE)

        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8), BUFFER_SIZE_CHARS)
        val result =
            PrintScriptRunner.execute(
                source = reader,
                version = version,
                output = counter,
                input = input,
            )

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals(MockInputStream.NUMBER_OF_LINES, counter.count)
    }

    @Test
    @DisplayName("Ejecución agota la memoria y lanza OutOfMemoryError usando PrintCollector")
    fun executionReportsJavaHeapSpaceErrorWhenMemoryIsExhaustedWithPrintCollector() {
        val stream = MockInputStream()
        var collector: PrintCollector? = PrintCollector()

        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8), BUFFER_SIZE_CHARS)
        var oomThrown = false
        try {
            PrintScriptRunner.execute(
                source = reader,
                version = version,
                output = collector!!,
                input = input,
            )
        } catch (e: OutOfMemoryError) {
            oomThrown = true
        } finally {
            collector = null
            System.gc()
        }

        assertTrue(oomThrown, "Expected OutOfMemoryError to be thrown")
    }

    companion object {
        // En JUnit 5 (Jupiter), el piso de la JVM antes de ejecutar el test es ~4,52 MB (frente a los
        // 4,02 MB medidos con JUnit 4 en el fork de validación).
        // Con heap de 7 MB (-Xmx7m, redondeado por G1 a 8 MB) y un punto de quiebre de ~7,3 MB:
        //   - Borde inferior medido: 256 K chars (512 KB) -> por debajo, PrintCollector no agota el heap.
        //   - Borde superior medido: 504 K chars (1008 KB) -> por encima, PrintCounter agota el heap.
        // Se elige el centro exacto de la ventana: 384 K chars (768 KB en memoria).
        const val BUFFER_SIZE_CHARS = 384 * 1024
    }
}
