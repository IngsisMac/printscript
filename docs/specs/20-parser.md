# Escenarios — Parser

Convenciones en [`00-convenciones.md`](00-convenciones.md).

Todos los escenarios unitarios de este archivo se ejecutan pasando un
`Iterator<Token>` fabricado a mano, **sin lexer**.

---

### PS-PAR-001 — Declaración con inicialización produce un VariableDeclaration
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number = 5;"
When se parsea
Then se obtiene un VariableDeclaration
And su nombre es "x", su tipo declarado es number y es mutable
And su inicializador es un NumberLiteral de valor 5
```

---

### PS-PAR-002 — Declaración sin inicialización es válida
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number;"
When se parsea
Then se obtiene un VariableDeclaration con inicializador nulo
```

---

### PS-PAR-003 — Asignación a variable ya declarada
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "x = 5;"
When se parsea
Then se obtiene un Assignment cuyo destino es el identificador "x"
```

---

### PS-PAR-004 — `println` produce un PrintlnStatement
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "println(\"hola\");"
When se parsea
Then se obtiene un PrintlnStatement
And su argumento es un StringLiteral de valor "hola"
```

---

### PS-PAR-005 — La multiplicación liga más fuerte que la suma
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number = 2 + 3 * 4;"
When se parsea
Then el inicializador es un BinaryExpression con operador +
And su operando izquierdo es NumberLiteral(2)
And su operando derecho es un BinaryExpression con operador *
```

> Este es el escenario que descarta el parseo plano izquierda-a-derecha. Ver ADR-0007.

---

### PS-PAR-006 — Los operadores de igual precedencia asocian a izquierda
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number = 10 - 3 - 2;"
When se parsea
Then el árbol resultante equivale a ((10 - 3) - 2)
```

---

### PS-PAR-007 — Los paréntesis cambian la precedencia
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number = (2 + 3) * 4;"
When se parsea
Then el operador raíz es *
```

---

### PS-PAR-008 — Falta el punto y coma
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "let x: number = 5"
When se parsea
Then se reporta un error de punto y coma faltante
And el span señala el final de la expresión
```

> Corresponde a `validation/1.0/invalid-missing-semi-colon.ps` del TCK.

---

### PS-PAR-009 — Paréntesis sin cerrar
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given los tokens de "println(\"hola\";"
When se parsea
Then se reporta un error de paréntesis sin cerrar con su posición
```

---

### PS-PAR-010 — `const` es inválido en 1.0
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un parser configurado para la versión 1.0
And la fuente "const x: number = 5;"
When se parsea
Then se reporta un error con su posición
```

> Corresponde a `validation/1.0/invalid-const-declaration.ps` del TCK. Ver ADR-0006.

---

### PS-PAR-011 — `if` es inválido en 1.0
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un parser configurado para la versión 1.0
And la fuente:
  """
  let a: boolean = true;
  if (a) { println("hola"); }
  """
When se parsea
Then se reporta un error con su posición
```

> Corresponde a `validation/1.0/invalid-if-statement.ps` del TCK.

---

### PS-PAR-012 — El parser entrega statements de a uno
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente con 32768 sentencias
When se pide el primer statement
Then el lexer no consumió más allá de la primera sentencia
And no existe ninguna colección con todos los statements
```

> Ver ADR-0003, reglas M1 y M2.

---

### PS-PAR-013 — El buffer de lookahead no crece con el archivo
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente con 32768 sentencias
When se consumen todos los statements
Then el tamaño del buffer de tokens nunca supera un máximo acotado
```

> Regla M2. Es el error clásico del `TokenBuffer` que acumula.

---

### PS-PAR-014 — `const` es válido en 1.1
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given un parser configurado para la versión 1.1
And la fuente "const x: number = 5;"
When se parsea
Then se obtiene un VariableDeclaration no mutable
```

---

### PS-PAR-015 — `if` con `else` produce el árbol esperado
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given un parser configurado para la versión 1.1
And la fuente:
  """
  if (flag) { println("si"); } else { println("no"); }
  """
When se parsea
Then se obtiene un IfStatement con bloque then y bloque else
```

---

### PS-PAR-016 — `else if` no está soportado
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given un parser configurado para la versión 1.1
And la fuente:
  """
  if (a) { println("1"); } else if (b) { println("2"); }
  """
When se parsea
Then se reporta un error con su posición
```
