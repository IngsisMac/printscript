# Escenarios — Memoria y streaming

Estos escenarios corren en la suite `memoryTest`, con `minHeapSize = "5m"` y
`maxHeapSize = "7m"`, replicando exactamente el entorno del TCK.

**Son los escenarios que definen si la entrega pasa.** Contexto completo en
`knowledge-base/context/03-reglas-de-memoria.md`.

---

### PS-MEM-001 — Archivo grande con emitter que no acumula
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente compuesta por 32768 repeticiones de:
  """
  println("This is a text");
  """
And un heap máximo de 7 MB
And un emitter que solo cuenta mensajes
When se ejecuta el programa
Then el emitter recibe exactamente 32768 mensajes
And cada mensaje es exactamente "This is a text"
And la lista de errores está vacía
```

> Réplica de `InterpreterLargeFileTest.testWithCounter`.
> **Filtra: ¿el pipeline es streaming?** Falla si alguna etapa materializa su salida.

**Cubierto por:** `runner/src/memoryTest/.../LargeFileTest.kt`

---

### PS-MEM-002 — Archivo grande con emitter que acumula
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente compuesta por 32768 repeticiones de:
  """
  println("This is a text");
  """
And un heap máximo de 7 MB
And un emitter que guarda todos los mensajes en una lista
When se ejecuta el programa
Then se reporta exactamente un error
And ese error es el string "Java heap space"
```

> Réplica de `InterpreterLargeFileTest.testWithCollector`.
> **Filtra: ¿se captura el `OutOfMemoryError`?** El OOM lo causa el emitter del test,
> no la implementación; hay que atraparlo y reportarlo.
>
> Recordar: `catch (e: Exception)` **no** atrapa `OutOfMemoryError`.

**Cubierto por:** `runner/src/memoryTest/.../LargeFileTest.kt`

---

### PS-MEM-003 — El mensaje del OOM se reporta sin decorar
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given las condiciones de PS-MEM-002
When se reporta el error
Then el string es exactamente "Java heap space"
And no lleva prefijo de posición ni de severidad
```

---

### PS-MEM-004 — No se emiten mensajes espurios por el handler de errores
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given las condiciones de PS-MEM-001
When se ejecuta el programa
Then el ErrorHandler no recibe ningún mensaje de progreso, warning ni diagnóstico
```

> El progreso del parsing es un requisito del CLI y vive en el módulo `cli`, nunca en la
> librería.

---

### PS-MEM-005 — Los lexemes no se comparten entre tokens
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la fuente:
  """
  println("igual");
  println("igual");
  """
When se leen los tokens
Then los dos STRING_LITERAL son instancias distintas de String
```

> Si se compartieran, `PrintCollector` guardaría 32768 referencias al mismo objeto, no
> habría OOM y PS-MEM-002 fallaría. Ver ADR-0010.

---

### PS-MEM-006 — El formatter escribe incrementalmente
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente de 32768 sentencias
And un heap máximo de 7 MB
And un Writer que descarta lo que recibe
When se formatea el programa
Then no se reporta ningún error
```

> Regla M5: el formatter no puede construir la salida completa en un `StringBuilder`.

---

## Utilidades de la suite

Copiadas del TCK para poder correr sin depender de él:

```kotlin
// Devuelve un char por llamada a read(); no sobreescribe read(byte[]).
class RepeatingInputStream(line: String, private val times: Int) : InputStream() {
    private val bytes = line.chars().toArray()
    private var index = 0
    private var lineNumber = 0

    override fun read(): Int {
        if (index == bytes.size) { index = 0; lineNumber++ }
        return if (lineNumber < times) bytes[index++] else -1
    }
}

class CountingEmitter(private val expected: String) : OutputEmitter {
    var count = 0; private set
    override fun emit(line: String) {
        require(line == expected) { "mensaje inesperado: $line" }
        count++
    }
}

class CollectingEmitter : OutputEmitter {
    val messages = mutableListOf<String>()
    override fun emit(line: String) { messages.add(line) }
}
```
