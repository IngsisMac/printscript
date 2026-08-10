# Escenarios — Formatter

Convenciones en [`00-convenciones.md`](00-convenciones.md).

> **Antes de implementar:** abrir los `config.json` de
> `printscript-tck/src/test/resources/formatter/{1.0,1.1}/` y copiar los nombres de las
> claves exactos. El modelo de configuración se escribe a partir de esos archivos.
> Los nombres de las carpetas del TCK son una pista fuerte y se usaron como nombre
> tentativo de cada regla acá.

Todos los escenarios siguen la misma forma: fuente + configuración → salida esperada,
con golden files en `runner/src/integrationTest/resources/cases/formatter/`.

---

### PS-FMT-001 — Espacio antes de los dos puntos, prendido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración con la regla de espacio antes de ":" en true
And la fuente "let x:number = 5;"
When se formatea
Then la salida es "let x : number = 5;"
```
*(TCK: `enforce-decl-spacing-before-colon`)*

---

### PS-FMT-002 — Espacio después de los dos puntos, prendido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración con la regla de espacio después de ":" en true
And la fuente "let x:number = 5;"
When se formatea
Then la salida es "let x: number = 5;"
```
*(TCK: `enforce-decl-spacing-after-colon`)*

---

### PS-FMT-003 — Espacio alrededor del igual, prendido
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración con la regla de espacio alrededor de "=" en true
And la fuente "let x: number=5;"
When se formatea
Then la salida es "let x: number = 5;"
```
*(TCK: `assign-spacing-surrounding-equals`)*

---

### PS-FMT-004 — Espacio alrededor del igual, apagado
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración con la regla de espacio alrededor de "=" en false
And la fuente "let x: number = 5;"
When se formatea
Then la salida es "let x: number=5;"
```
*(TCK: `assign-no-spacing-surrounding-equals`)*

---

### PS-FMT-005 — Saltos de línea antes de println: 0, 1 y 2
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la configuración con n saltos de línea antes de println
And una fuente con una declaración seguida de un println
When se formatea
Then hay exactamente n líneas en blanco antes del println
```
*(TCK: `print-0-line-breaks-after`, `print-1-line-breaks-after`, `print-2-line-breaks-after`)*

Se escribe como escenario parametrizado con n ∈ {0, 1, 2}.

---

### PS-FMT-006 — Salto de línea después de cada punto y coma (no configurable)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la fuente "let a: number = 1; let b: number = 2;"
When se formatea con cualquier configuración
Then cada sentencia queda en su propia línea
```
*(TCK: `line-break-after-statement-enforced`)*

---

### PS-FMT-007 — Un solo espacio entre tokens (no configurable)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la fuente "let     x  :  number   =   5;"
When se formatea
Then nunca hay dos espacios consecutivos entre tokens
```
*(TCK: `enforce-single-space-separation`)*

---

### PS-FMT-008 — Espacio alrededor de los operadores (no configurable)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given la fuente "let x: number = 1+2*3;"
When se formatea
Then la salida es "let x: number = 1 + 2 * 3;"
```
*(TCK: `enforce-space-surrounding-operations`)*

---

### PS-FMT-009 — Formatear dos veces da el mismo resultado (idempotencia)
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given cualquier fuente y cualquier configuración
When se formatea la salida ya formateada
Then el resultado es idéntico
```

> No lo pide la consigna, pero es la propiedad que detecta la mayoría de los bugs de
> formatter con un solo test.

---

### PS-FMT-010 — Indentación configurable dentro de un bloque if
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given la configuración con indentación de 2 espacios
And una fuente con un if que contiene un println
When se formatea
Then el contenido del bloque está indentado 2 espacios respecto del if
```
*(TCK: `if-indent-inside-2`)*

---

### PS-FMT-011 — La llave del if en la misma línea
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given la configuración con la llave en la misma línea
And una fuente con un if
When se formatea
Then la "{" queda en la misma línea que el if
```
*(TCK: `if-brace-same-line`)*

---

### PS-FMT-012 — La llave del if en la línea siguiente
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given la configuración con la llave en la línea siguiente
And una fuente con un if
When se formatea
Then la "{" queda en la línea siguiente al if
```
*(TCK: `if-brace-below-line`)*

> La consigna 2025 dice que la llave va "en la misma línea", pero el TCK trae los dos
> casos: la posición es **configurable**.

---

### PS-FMT-013 — El formatter escribe a un Writer, no devuelve un String
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ⬜

Ver [`70-memoria.md`](70-memoria.md), escenario PS-MEM-006.
