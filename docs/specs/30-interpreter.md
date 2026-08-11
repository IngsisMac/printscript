# Escenarios — Interpreter

Convenciones en [`00-convenciones.md`](00-convenciones.md).

---

### PS-INT-001 — Concatenación de strings (ejemplo 1 de la consigna)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un programa PrintScript 1.0:
  """
  let name: string = "Joe";
  let lastName: string = "Doe";

  println(name + " " + lastName);
  """
When se ejecuta con el Interpreter
Then el emitter recibe exactamente una línea: "Joe Doe"
And no se reporta ningún error
```
**Caso:** `print-statement/1.0/001-string-concat`

---

### PS-INT-002 — División que da entero exacto (ejemplo 2 de la consigna)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un programa PrintScript 1.0:
  """
  let a: number = 12;
  let b: number = 4;
  let c: number = a / b;

  println("Result: " + c);
  """
When se ejecuta con el Interpreter
Then el emitter recibe exactamente una línea: "Result: 3"
And no se reporta ningún error
```

> **"Result: 3", no "Result: 3.0".** Ver ADR-0008.

**Caso:** `print-statement/1.0/002-division-entera`

---

### PS-INT-003 — Reasignación con división (ejemplo 3 de la consigna)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un programa PrintScript 1.0:
  """
  let a: number = 12;
  let b: number = 4;
  a = a / b;

  println("Result: " + a);
  """
When se ejecuta con el Interpreter
Then el emitter recibe exactamente una línea: "Result: 3"
```
**Caso:** `print-statement/1.0/003-reasignacion`

---

### PS-INT-004 — El emitter recibe el mensaje sin salto de línea
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa "println(\"hola\");"
When se ejecuta
Then el emitter recibe exactamente el string "hola"
And el string no termina en "\n"
```

> `PrintCounter` del TCK lanza excepción si el mensaje no coincide exactamente. Ver
> trampa T3.

---

### PS-INT-005 — La aritmética decimal es exacta
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let a: number = 0.1;
  let b: number = 0.2;
  println(a + b);
  """
When se ejecuta
Then el emitter recibe "0.3"
```

> Con `Double` daría `0.30000000000000004`. Ver ADR-0008.

---

### PS-INT-006 — La división no terminante no explota
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa "println(1 / 3);"
When se ejecuta
Then no se lanza ninguna excepción
And el emitter recibe un decimal con la precisión de DECIMAL64
```

> `BigDecimal.divide` sin `MathContext` lanza `ArithmeticException`. Trampa T8.

---

### PS-INT-007 — Concatenación de string con number produce string
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let n: number = 5;
  println("valor: " + n);
  """
When se ejecuta
Then el emitter recibe "valor: 5"
```

---

### PS-INT-008 — Usar una variable no declarada es un error posicionado
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa "println(x);"
When se valida
Then se reporta un error de variable no declarada
And el span señala el identificador "x"
```

---

### PS-INT-009 — Asignar un tipo incompatible es un error
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa "let x: number = \"hola\";"
When se valida
Then se reporta un error de tipo incompatible con su posición
```

> Corresponde a `validation/1.0/invalid-expression-for-type.ps` del TCK.

---

### PS-INT-010 — Operar aritméticamente sobre strings es un error
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let a: string = "hola";
  let b: string = "chau";
  println(a - b);
  """
When se valida
Then se reporta un error con su posición
```

> Corresponde a `validation/1.0/invalid-string-arithmetic-op.ps` del TCK.
> Nota: `+` sobre dos strings **sí** es válido (concatenación); `-`, `*` y `/` no.

---

### PS-INT-011 — Redeclarar una variable es un error
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let x: number = 1;
  let x: number = 2;
  """
When se valida
Then se reporta un error de variable ya declarada con su posición
```

---

### PS-INT-012 — La validación no ejecuta
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el programa "println(\"hola\");"
When se corre en modo Validation
Then el emitter no recibe ningún mensaje
And no se reporta ningún error
```

---

### PS-INT-013 — Reasignar una constante es un error
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given el programa:
  """
  const x: number = 1;
  x = 2;
  """
When se valida
Then se reporta un error de reasignación de constante con su posición
```

> Corresponde a `validation/1.1/invalid-const-re-assign.ps` del TCK.

---

### PS-INT-014 — `if` ejecuta el bloque correcto
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let flag: boolean = true;
  if (flag) { println("si"); } else { println("no"); }
  """
When se ejecuta
Then el emitter recibe exactamente una línea: "si"
```

---

### PS-INT-015 — La condición del `if` debe ser booleana
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given el programa:
  """
  let n: number = 1;
  if (n) { println("si"); }
  """
When se valida
Then se reporta un error con su posición
```

> Corresponde a `validation/1.1/invalid-argument-in-if.ps` del TCK.

---

### PS-INT-016 — `readInput` toma el valor del proveedor
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given un InputSource que devuelve "42"
And el programa:
  """
  let n: number = readInput("Ingresá un número: ");
  println(n);
  """
When se ejecuta
Then el emitter recibe "Ingresá un número: " y luego "42"
```

---

### PS-INT-017 — `readInput` falla si el valor no es del tipo esperado
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given un InputSource que devuelve "Hola"
And el programa "let flag: boolean = readInput(\"dame un boolean: \");"
When se ejecuta
Then se reporta un error de conversión de tipo
```

---

### PS-INT-018 — `readEnv` lee una variable de ambiente
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given la variable de ambiente BEST_FOOTBALL_CLUB con valor "San Lorenzo"
And el programa "println(readEnv(\"BEST_FOOTBALL_CLUB\"));"
When se ejecuta
Then el emitter recibe "San Lorenzo"
```

> El TCK setea esta variable en su `build.gradle`.

---

### PS-INT-019 — El intérprete no acumula statements
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una fuente de 32768 sentencias println
And un heap de 7 MB
When se ejecuta con un emitter que no acumula
Then se emiten 32768 mensajes
And no se reporta ningún error
```

> Réplica de `InterpreterLargeFileTest.testWithCounter`. Ver
> `knowledge-base/context/03-reglas-de-memoria.md`. Detalle en
> [`70-memoria.md`](70-memoria.md).
