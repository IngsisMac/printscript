# Escenarios — Static code analyzer (Linter)

Convenciones en [`00-convenciones.md`](00-convenciones.md).

> **Antes de implementar:** copiar los nombres exactos de las claves desde
> `printscript-tck/src/test/resources/linter/{1.0,1.1}/*/config.json`.

El linter **acumula** violaciones: no corta en la primera. Cada violación lleva su
posición exacta, igual que un error (lo pide la consigna explícitamente).

---

### PS-LNT-001 — Configuración vacía no reporta nada
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una configuración sin ninguna regla activa
And una fuente cualquiera, sintácticamente válida
When se analiza
Then no se reporta ninguna violación
```
*(TCK: `valid-no-rules`)*

> Una configuración vacía no debe romper. Por defecto todas las reglas están apagadas.

---

### PS-LNT-002 — Identificadores en camel case, válido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración de identificadores en "camel case"
And la fuente:
  """
  let myVariable: number = 5;
  """
When se analiza
Then no se reporta ninguna violación
```
*(TCK: `valid-mandatory-camel-case-identifiers`)*

---

### PS-LNT-003 — Identificadores en camel case, inválido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración de identificadores en "camel case"
And la fuente:
  """
  let my_variable: number = 5;
  """
When se analiza
Then se reporta una violación
And la posición señala el identificador "my_variable"
```
*(TCK: `invalid-mandatory-camel-case-identifiers`)*

---

### PS-LNT-004 — Identificadores en snake case, válido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración de identificadores en "snake case"
And la fuente "let my_variable: number = 5;"
When se analiza
Then no se reporta ninguna violación
```
*(TCK: `valid-mandatory-snake-case-identifiers`)*

---

### PS-LNT-005 — Identificadores en snake case, inválido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración de identificadores en "snake case"
And la fuente "let myVariable: number = 5;"
When se analiza
Then se reporta una violación con la posición del identificador
```
*(TCK: `invalid-mandatory-snake-case-identifiers`)*

---

### PS-LNT-006 — `println` con una expresión, prohibido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la regla de argumento simple en println prendida
And la fuente:
  """
  let a: number = 1;
  let b: number = 2;
  println(a + b);
  """
When se analiza
Then se reporta una violación
And la posición señala el argumento del println
```
*(TCK: `invalid-println-with-expression`)*

---

### PS-LNT-007 — `println` con un identificador o un literal, permitido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la regla de argumento simple en println prendida
And la fuente:
  """
  let a: number = 1;
  println(a);
  println("hola");
  println(42);
  """
When se analiza
Then no se reporta ninguna violación
```

---

### PS-LNT-008 — La regla de println se puede apagar
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la regla de argumento simple en println apagada
And la fuente "println(1 + 2);"
When se analiza
Then no se reporta ninguna violación
```

---

### PS-LNT-009 — Se acumulan todas las violaciones, no solo la primera
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración de identificadores en "camel case"
And la fuente:
  """
  let mal_uno: number = 1;
  let mal_dos: number = 2;
  """
When se analiza
Then se reportan exactamente dos violaciones
And cada una señala su propio identificador
```

> Este escenario es la razón de ADR-0005 (errores como valor).

---

### PS-LNT-010 — Una regla desconocida en la configuración es un error claro
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una configuración con una clave que no corresponde a ninguna regla
When se construye el linter
Then se reporta un error indicando la clave desconocida
```

---

### PS-LNT-011 — `readInput` con una expresión, prohibido
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given la regla de argumento simple en readInput prendida
And una fuente donde readInput recibe una concatenación
When se analiza
Then se reporta una violación con su posición
```
*(TCK: `invalid-read-input-with-expression`)*

---

### PS-LNT-012 — El linter no acumula el programa en memoria
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given una fuente de 32768 sentencias válidas
And un heap máximo de 7 MB
When se analiza con todas las reglas prendidas
Then no se reporta ninguna violación ni error
```

> El linter recorre el `Iterator<Statement>` y descarta cada nodo. Regla M1.
