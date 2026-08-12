package com.printscript.runner

import com.printscript.common.InputSource
import com.printscript.common.OutputEmitter
import com.printscript.common.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.ArrayList

@Disabled("Desactivado temporalmente a pedido del usuario hasta implementar mas alla de los happy paths")
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
    @DisplayName("PS-MEM-001 | Ejecución streaming en pipeline con heap de 7MB")
    fun testWithCounterStreamingPipelineExecutionWith7MBHeap() {
        val stream = MockInputStream()
        val counter = PrintCounter(MockInputStream.MESSAGE)

        val result =
            PrintScriptRunner.execute(
                source = stream.reader(),
                version = version,
                output = counter,
                input = input,
            )

        assertTrue(result.errors.isEmpty(), "Expected no errors but got: ${result.errors}")
        assertEquals(MockInputStream.NUMBER_OF_LINES, counter.count)
    }

    @Test
    @DisplayName("PS-MEM-002 | Captura de OutOfMemoryError reportando Java heap space")
    fun testWithCollectorCapturesOutOfMemoryErrorAndReportsJavaHeapSpace() {
        val stream = MockInputStream(numberOfLines = 64 * 1024)
        val collector = PrintCollector()

        val result =
            PrintScriptRunner.execute(
                source = stream.reader(),
                version = version,
                output = collector,
                input = input,
            )

        assertEquals(1, result.errors.size, "Expected exactly 1 error on OOM, got: ${result.errors}")
        assertEquals("Java heap space", result.errors[0].message)
    }
}
