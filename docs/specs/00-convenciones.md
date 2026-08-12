# Convenciones de los escenarios

Los archivos de esta carpeta son la **especificación viva** de PrintScript: describen el
comportamiento esperado en Given / When / Then antes de implementarlo.

No son tests ejecutables. No se usa Cucumber. La trazabilidad con el código de test se
logra con un **ID compartido**.

## Formato de un escenario

````markdown
### PS-INT-002 — División de números que da entero exacto
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜ pendiente

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
**Cubierto por:** `runner/src/integrationTest/.../print-statement/1.0/002-division-entera`
````

Y en el test correspondiente:

```kotlin
@Test
@DisplayName("PS-INT-002 | División exacta imprime entero sin decimales")
fun divisionExactaImprimeEntero() { ... }
```

`grep PS-INT-002` lleva del escenario al test y del test al escenario.

## Prefijos de ID

| Prefijo | Componente |
|---|---|
| `PS-LEX` | Lexer |
| `PS-PAR` | Parser |
| `PS-INT` | Interpreter |
| `PS-FMT` | Formatter |
| `PS-LNT` | Linter |
| `PS-CLI` | CLI |
| `PS-MEM` | Memoria y streaming |

Los IDs **no se reutilizan** aunque un escenario se elimine.

## Estados

| Símbolo | Significado |
|---|---|
| ⬜ | Pendiente: escrito, sin implementar |
| 🟨 | En progreso: hay test, todavía falla |
| ✅ | Implementado y en verde |
| ⛔ | Descartado (se deja el ID con la razón) |

## Niveles

- **unitario** — un solo módulo, sin tocar disco.
- **integración** — cruza módulos, o usa golden files del directorio `cases/`.
- **memoria** — corre en la suite `memoryTest` con heap de 7 MB.
- **aceptación** — lo cubre el TCK.

## Reglas

1. El escenario se escribe **antes** que el test, y el test antes que la
   implementación.
2. Un escenario describe **un** comportamiento observable. Si tiene tres `Then`
   independientes, son tres escenarios.
3. El `Then` describe lo que ve el usuario o el consumidor de la API, no la estructura
   interna. "El emitter recibe X", no "el AST tiene un nodo Y".
4. Los escenarios de error especifican **la posición esperada**, porque la consigna lo
   exige.
5. Cuando un escenario pasa a ✅, se anota qué test lo cubre.
6. Todos los tests en código Kotlin deben usar `@BeforeEach` para preparar fixtures/componentes y estructurar el caso con escenario Given-When-Then implícito (separado visualmente por saltos de línea, **sin comentarios explícitos** `// Given`, `// When`, `// Then`).

