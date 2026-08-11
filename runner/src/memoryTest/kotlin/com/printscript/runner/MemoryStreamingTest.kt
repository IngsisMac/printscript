package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.ArrayList

/**
 * Tests de streaming y memoria (suite memoryTest con max heap 7 MB).
 * PS-MEM-001: Valida que el pipeline sea streaming de punta a punta (32.768 impresiones sin OOM).
 * PS-MEM-002: Valida la captura de OutOfMemoryError produciendo la cadena "Java heap space".
 *
 * TODO: Deshardcodear los datos de este test (mensajes/líneas/número de repeticiones) en el futuro
 * para parametrizar y expandir con casos adicionales para incrementar la cobertura.
 */
class MemoryStreamingTest {

    class PrintCounter(private val expectedMessage: String) : OutputEmitter {
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
    fun `testWithCounter - streaming pipeline execution with 7MB heap`() {
        val stream = MockInputStream()
        val counter = PrintCounter(MockInputStream.MESSAGE)
        val input = InputSource { "" }

        val result = PrintScriptRunner.execute(
            source = stream.reader(),
            version = Version.V1_0,
            output = counter,
            input = input,
        )

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals(MockInputStream.NUMBER_OF_LINES, counter.count)
    }

    @Test
    fun `testWithCollector - captures OutOfMemoryError and reports Java heap space`() {
        // Usar 64K líneas para garantizar el desborde de memoria (heap max 7 MB) al acumular en PrintCollector
        val stream = MockInputStream(numberOfLines = 64 * 1024)
        val collector = PrintCollector()
        val input = InputSource { "" }

        val result = PrintScriptRunner.execute(
            source = stream.reader(),
            version = Version.V1_0,
            output = collector,
            input = input,
        )

        assertEquals(1, result.errors.size, "Expected exactly 1 error on OOM, got: ${result.errors}")
        assertEquals("Java heap space", result.errors[0].message)
    }
}
