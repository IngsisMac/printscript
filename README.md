# PrintScript

Toolchain e intérprete completo para el lenguaje **PrintScript**, desarrollado en **Kotlin** bajo una arquitectura monorrepo multi-módulo con **Gradle**.

El sistema implementa análisis léxico, parsing con algoritmo Pratt, ejecución e interpretación semántica con scoping léxico, formateo configurable de código, análisis estático (linter) basado en reglas y una interfaz de línea de comandos (CLI) productiva. Todo el diseño está optimizado bajo un **pipeline de streaming perezoso de punta a punta**, garantizando un consumo de memoria acotado $O(1)$ capaz de procesar archivos de gran escala en entornos con memoria restringida (hasta 7 MB de heap).

---

## 📑 Índice

1. [Arquitectura General y Pipeline de Streaming](#-arquitectura-general-y-pipeline-de-streaming)
2. [Desglose de Diseño por Módulo](#-desglose-de-diseño-por-módulo)
   - [`common`](#1-common)
   - [`token`](#2-token)
   - [`ast`](#3-ast)
   - [`lexer`](#4-lexer)
   - [`parser`](#5-parser)
   - [`interpreter`](#6-interpreter)
   - [`formatter`](#7-formatter)
   - [`linter`](#8-linter)
   - [`runner`](#9-runner)
   - [`cli`](#10-cli)
3. [Catálogo de Patrones de Diseño](#-catálogo-de-patrones-de-diseño)
4. [PrintScript CLI — Comandos y Uso](#-printscript-cli--comandos-y-uso)
5. [Comandos de Build y Testing (Gradle)](#-comandos-de-build-y-testing-gradle)
6. [Especificación del Lenguaje y Versiones](#-especificación-del-lenguaje-y-versiones)
7. [Estructura del Proyecto](#-estructura-del-proyecto)

---

## 🏗 Arquitectura General y Pipeline de Streaming

El sistema se estructura en **10 módulos desacoplados**, donde cada etapa del pipeline depende únicamente del **contrato formal** de la etapa anterior y nunca de su implementación concreta:

```
                         common
                    (sin dependencias)
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   │
      token                ast ─────────────────┤
        │                   │                   │
        ▼                   │                   │
      lexer                 │                   │
        │                   │                   │
        ▼                   │                   │
      parser ───────────────┤                   │
        │                   │                   │
        ▼                   ▼                   ▼
   interpreter          formatter            linter
        └───────────────────┼───────────────────┘
                            ▼
                         runner   ← Fachada pública
                            ▼
                           cli    ← Entrada de usuario
```

### Invariantes de Streaming y Memoria ($O(1)$)
- **Sin nodo raíz `Program`:** El programa es representado como un flujo perezoso `Iterator<Statement>`. No existe una estructura de datos que acumule el archivo completo en memoria.
- **Consumo bajo demanda:** El `Lexer` procesa carácter por carácter mediante `CharStream` con lookahead de 1 símbolo. El `Parser` mantiene un buffer de lookahead acotado a 2 tokens (`TokenStream`).
- **Procesamiento Statement por Statement:** El intérprete, formateador y linter consumen, procesan y liberan cada sentencia secuencialmente antes de solicitar la siguiente.

---

## 🧩 Desglose de Diseño por Módulo

### 1. `common`
*El vocabulario compartido y los contratos fundamentales del sistema.*

- **Responsabilidad:** Define las estructuras base de posicionamiento, rangos de código, versión del lenguaje, modelos de error y las abstracciones funcionales de entrada/salida. No posee dependencias internas ni externas.
- **Componentes Clave:**
  - `Position`: Representa coordenadas `(line, column)` con invariante $\ge 1$.
  - `Span`: Rango inmutable `(start, end)` para asociar ubicación exacta en el código fuente a tokens, nodos y errores.
  - `PrintScriptError`: Modelo unificado de errores con renderizado determinista `"[line:col]-[line:col] Error: <mensaje>"`.
  - `Version`: Enum exhaustivo (`V1_0`, `V1_1`) con resolución segura mediante `Result<Version>`.
  - `OutputEmitter`, `InputSource`, `EnvSource`: Interfaces funcionales (`fun interface`) para inversión de dependencias pura en I/O.
- **Decisiones de Diseño:**
  - Validaciones estrictas en `init` de `Position` y `Span` para detectar inconsistencias de posición en fases tempranas.
  - Las interfaces I/O desacoplan el motor de la consola estándar, permitiendo redirigir streams a tests unitarios, WebSockets o entornos embebidos.

---

### 2. `token`
*El contrato entre el análisis léxico y el parsing.*

- **Responsabilidad:** Define los tipos de tokens y la estructura atómica producida por el lexer.
- **Componentes Clave:**
  - `TokenType`: Enum de 27 variantes categorizadas (palabras clave `LET`, `CONST`, `PRINTLN`, `IF`, `ELSE`; tipos `TYPE_NUMBER`, `TYPE_STRING`, `TYPE_BOOLEAN`; literales; operadores aritméticos y asignación; delimitadores; y `EOF`).
  - `Token`: Data class inmutable `(type, lexeme, span)`.
- **Decisiones de Diseño:**
  - Separado en un módulo independiente para que `parser` no dependa de la implementación de `lexer`.
  - Token `EOF` sintetizado por el stream para simplificar las condiciones de corte del parser sin requerir manejo de nulos.
  - Lexemas instanciados como strings individuales sin caché global para garantizar la recolección de basura durante el streaming.

---

### 3. `ast`
*La jerarquía del Árbol de Sintaxis Abstracta y el contrato de recorrido.*

- **Responsabilidad:** Modela los nodos de sintaxis abstracta del lenguaje mediante jerarquías selladas (`sealed`). Depende exclusivamente de `common`.
- **Componentes Clave:**
  - `AstNode`: Interfaz sellada base que expone `span: Span`.
  - `Statement`: Sentencias ejecutables (`Declaration`, `Assignment`, `PrintStatement`, `IfStatement`).
  - `Expression`: Expresiones evaluables (`NumberLiteral`, `StringLiteral`, `BooleanLiteral`, `Variable`, `BinaryOp`, `CallExpression`).
  - `AstVisitor<R>`: Interfaz para recorrido extensible mediante el patrón Visitor.
- **Decisiones de Diseño:**
  - **Ausencia de nodo `Program`:** Garantiza estructuralmente la imposibilidad de retener todo el AST en memoria.
  - `NumberLiteral.value` preserva el lexema textual exacto (ej. `"1.50"`), permitiendo al formateador conservar el estilo y delegando la aritmética al intérprete.
  - Tipos sellados (`sealed class` / `sealed interface`) que garantizan exhaustividad en tiempo de compilación para expresiones `when`.

---

### 4. `lexer`
*Analizador léxico perezoso y streaming de caracteres.*

- **Responsabilidad:** Transforma un `Reader` en un flujo `Iterator<Token>` carácter por carácter, reconociendo palabras clave según la versión activa.
- **Componentes Clave:**
  - `CharStream`: Abstracción sobre `Reader` con lookahead de 1 carácter (`peek()`) y seguimiento de línea y columna.
  - `TokenMatcher`: Interfaz **Strategy** para reconocimiento de tokens.
  - Matchers concretos: `SymbolMatcher`, `StringMatcher`, `NumberMatcher`, `IdentifierMatcher`.
  - `TokenConfig`: Proveedor de palabras clave parametrizado por `Version`.
  - `Lexer`: Orquestador que implementa `Iterator<Token>`.
- **Decisiones de Diseño:**
  - **Patrón Strategy:** Agregar soporte para nuevos símbolos o comentarios requiere añadir un matcher a la cadena sin modificar la lógica del `Lexer` (Open/Closed Principle).
  - **Versionado unificado:** En versión 1.0, tokens como `const` se reconocen como identificadores y el parser genera un error semántico contextual descriptivo.

---

### 5. `parser`
*Parsing híbrido: Precedence Climbing (Pratt) para expresiones y Strategy para sentencias.*

- **Responsabilidad:** Convierte un `Iterator<Token>` en un flujo `Iterator<Statement>`, validando la gramática y emitiendo errores con posición exacta.
- **Componentes Clave:**
  - `TokenStream`: Buffer circular acotado con operaciones `peek()`, `peekNext()`, `consume()` y `match()`.
  - `Parser`: Fachada que expone `parse(): Iterator<Statement>` mediante corrutinas de generación perezosa (`sequence { }`).
  - `StatementParser`: Estrategias para sentencias (`DeclarationStatementParser`, `AssignmentStatementParser`, `PrintStatementParser`, `IfStatementParser`).
  - `ExpressionParser`: Motor de parsing Pratt basado en tablas de binding powers.
  - `PrefixParser` (7 implementaciones) e `InfixParser` (`BinaryOperatorInfixParser`).
- **Decisiones de Diseño:**
  - **Pratt Parser para expresiones:** La precedencia y asociatividad izquierda se resuelven mediante una tabla numérica de binding powers (`left / left + 1`), simplificando la adición de nuevos operadores sin anidar métodos recursivos artificiales.
  - **Buffer acotado:** Lookahead máximo de 2 tokens en memoria, preservando el límite $O(1)$.

---

### 6. `interpreter`
*Motor de ejecución semántica, validación de tipos y manejo de scopes.*

- **Responsabilidad:** Evalúa `Iterator<Statement>` sobre un entorno jerárquico de variables, efectuando operaciones de I/O a través de interfaces abstractas.
- **Componentes Clave:**
  - `Interpreter`: Orquestador principal de ejecución y validación.
  - `InterpreterContext`: Interfaz de **Inversión de Dependencias (DIP)** que desacopla al intérprete de los evaluadores.
  - `Environment`: Árbol de scopes léxicos con soporte para shadowing y resolución en cadenas de padres.
  - `Symbol`: Representación de variables con estado `isConst`, `type`, `isInitialized` y `value`.
  - `Value`: Jerarquía sellada (`NumberValue`, `StringValue`, `BooleanValue`).
  - `StatementEvaluator<T>` y `ExpressionEvaluator<T>`: Evaluadores modulares por tipo de nodo.
- **Decisiones de Diseño:**
  - **Aritmética de alta precisión:** `NumberValue` utiliza `BigDecimal` con `MathContext.DECIMAL64` y formateo sin sufijos `.0` innecesarios para enteros exactos.
  - **Modo Dual (Ejecución vs. Validación):** El flag `isValidationMode` permite validar tipos, inicializaciones y scopes recorriendo ambas ramas condicionales sin emitir efectos colaterales de I/O.

---

### 7. `formatter`
*Formateador de código fuente configurable basado en reglas de estilo.*

- **Responsabilidad:** Reescribe sentencias hacia un `Writer` aplicando reglas de espaciado, saltos de línea e indentación.
- **Componentes Clave:**
  - `Formatter` / `DefaultFormatter`: Motor de formateo statement por statement.
  - `ExpressionFormatter`: Reconstructor de expresiones con recálculo determinista de precedencia para insertar paréntesis mínimos necesarios.
  - `FormatterConfig`: Configuración inmutable construida desde mapas/JSON compatibles con el TCK oficial.
- **Decisiones de Diseño:**
  - **Streaming a `Writer`:** No utiliza buffers globales de texto, escribiendo directamente al stream de salida.
  - **Prioridad determinista:** Resolución explícita ante reglas contradictorias de espaciado o apertura de llaves.

---

### 8. `linter`
*Analizador estático extensible basado en reglas y recorrido de AST.*

- **Responsabilidad:** Inspecciona el flujo de sentencias y reporta violaciones de buenas prácticas y convenciones de estilo.
- **Componentes Clave:**
  - `Linter` / `DefaultLinter`: Orquestador del análisis.
  - `AstVisitorLinter`: Implementación del patrón **Visitor** para recorrido sistemático de nodos.
  - `LinterRule`: Interfaz **Strategy** para reglas individuales (`IdentifierFormatRule`, `PrintlnExpressionRule`, `ReadInputExpressionRule`, `NoEmptyPrintlnRule`, `NoUnusedVariablesRule`).
  - `LinterConfig`: Modelo de configuración y mapeo de reglas.
  - `IdentifierFormat`: Validador de nomenclatura (`camelCase`, `snake_case`).
- **Decisiones de Diseño:**
  - **Auto-activación:** Cada regla consulta la configuración y devuelve una lista vacía si está desactivada, permitiendo configuraciones parciales o vacías sin fallos.
  - **Reglas oficiales + personalizadas:** Extensibilidad total para incorporar reglas estáticas adicionales.

---

### 9. `runner`
*Fachada pública del toolchain.*

- **Responsabilidad:** Expone los métodos de alto nivel para ejecutar, validar, formatear y analizar código PrintScript, unificando el montaje del pipeline.
- **Componentes Clave:**
  - `PrintScriptRunner`: Singleton (`object`) sin estado con métodos `execute()`, `validate()`, `format()` y `analyze()`.
  - `ExecutionResult`: Contenedor unificado de errores y diagnóstico de fallos fatales.
  - `OOM_RESULT`: Instancia preasignada para captura y reporte seguro de `OutOfMemoryError` sin incurrir en nuevas asignaciones de memoria.
- **Decisiones de Diseño:**
  - **API agnóstica:** Totalmente desacoplada de librerías externas o frameworks de test; recibe `Reader`, `Writer` e interfaces I/O funcionales.
  - Soporta sobrecargas directas para recibir configuraciones como `Map<String, Any?>` o como streams `Reader` en formato JSON.

---

### 10. `cli`
*Interfaz de línea de comandos productiva basada en PicoCLI.*

- **Responsabilidad:** Parseo de argumentos, lectura de archivos, códigos de salida y manejo de flujos estándar de consola.
- **Componentes Clave:**
  - `PrintScriptCli`: Comando raíz (`printscript`).
  - `ExecuteCommand`: Subcomando `execute`.
  - `FormatCommand`: Subcomando `format`.
  - `AnalyzeCommand`: Subcomando `analyze`.
  - `ConfigLoader`: Parser robusto de configuración JSON sin dependencias pesadas de serialización.
- **Decisiones de Diseño:**
  - **Testeabilidad:** Inyección de `PrintWriter` de salida y error a través de `spec.commandLine().out/err` para testear comandos sin alterar variables globales de la JVM.
  - **Códigos de salida semánticos:** `0` (éxito), `1` (errores en el programa PrintScript analizado/ejecutado), `2` (error de uso del CLI o archivo no encontrado).

---

## 🏛 Catálogo de Patrones de Diseño

| Patrón | Módulos y Clases Principales | Problema que Resuelve |
|---|---|---|
| **Iterator** | `Lexer`, `Parser.parse()` | Streaming perezoso $O(1)$ entre etapas del pipeline. |
| **Strategy** | `TokenMatcher`, `StatementParser`, `PrefixParser`, `InfixParser`, `LinterRule`, `StatementEvaluator`, `ExpressionEvaluator` | Extensibilidad Open/Closed: añadir operadores, sentencias o reglas sin modificar clases existentes. |
| **Pratt Parser** | `ExpressionParser` | Parsing de expresiones y asociatividad mediante binding powers numéricos. |
| **Facade** | `PrintScriptRunner`, `Parser` | Punto de entrada unificado y simple para subsistemas complejos. |
| **Visitor** | `AstVisitor<R>`, `AstVisitorLinter` | Recorrido tipado y extensible sobre la jerarquía sellada del AST. |
| **Dependency Inversion** | `InterpreterContext`, `OutputEmitter`, `InputSource` | Desacoplamiento de I/O y resolución de dependencias circulares. |
| **Factory / Provider** | `TokenConfig.from()`, `FormatterConfig.fromMap()`, `LinterConfig.fromMap()` | Configuración tipada según versión del lenguaje y fuentes externas. |
| **Composite** | `IfStatement` con listas de `Statement` | Representación de bloques anidados preservando la jerarquía sintáctica. |

---

## 💻 PrintScript CLI — Comandos y Uso

### 1. Sintaxis General

```bash
printscript <comando> <archivo> [opciones]
```

Opciones globales:
- `-h, --help`: Muestra la ayuda interactiva y la lista de comandos.
- `-V, --version`: Muestra la versión del CLI.

---

### 2. Subcomandos Disponibles

#### `execute` — Ejecución de programas
Ejecuta un archivo fuente `.ps` mostrando la salida estándar e interactuando mediante consola.

```bash
printscript execute <archivo.ps> [-v <1.0|1.1>]
```

**Ejemplo:**
```bash
printscript execute main.ps --version 1.1
```

---

#### `format` — Formateo de código fuente
Aplica reglas de estilo y escribe el resultado formateado directamente en la salida estándar.

```bash
printscript format <archivo.ps> [-c <config.json>] [-v <1.0|1.1>]
```

**Ejemplo:**
```bash
printscript format src/app.ps --config config/formatter.json --version 1.0
```

---

#### `analyze` — Análisis estático (Linter)
Verifica el cumplimiento de reglas de estilo y buenas prácticas, reportando violaciones con fila y columna.

```bash
printscript analyze <archivo.ps> [-c <config.json>] [-v <1.0|1.1>]
```

**Ejemplo:**
```bash
printscript analyze src/app.ps --config config/linter.json --version 1.1
```

---

### 3. Ejemplo de Archivos de Configuración JSON

#### Formatter (`formatter.json`)
```json
{
  "enforce-spacing-around-equals": true,
  "enforce-spacing-before-colon-in-declaration": false,
  "enforce-spacing-after-colon-in-declaration": true,
  "line-breaks-after-println": 1,
  "if-brace-below-line": false,
  "indent-inside-if": 4
}
```

#### Linter (`linter.json`)
```json
{
  "identifier_format": "camelCase",
  "mandatory-variable-or-literal-in-println": true,
  "mandatory-variable-or-literal-in-read-input": true
}
```

---

## ⚙ Comandos de Build y Testing (Gradle)

El monorrepo utiliza plugins de convención en `buildSrc` para estandarizar toolchains, suites de testeo, análisis estático y cobertura.

### Tareas Principales de Construcción y Verificación

```bash
# Compilación completa y ejecución de verificación de calidad
./gradlew check

# Construcción de artefactos JAR de todos los submódulos
./gradlew build
```

### Suites de Testing Especializadas (`jvm-test-suite`)

```bash
# Ejecución de tests unitarios aislados por módulo
./gradlew test

# Ejecución de tests de integración del pipeline completo
./gradlew integrationTest

# Suite de pruebas de memoria y streaming (ejecutada con 7 MB de heap máxima)
./gradlew memoryTest
```

### Calidad de Código, Formato y Cobertura

```bash
# Verificación de formato de código con Ktlint (Spotless)
./gradlew spotlessCheck

# Aplicación automática de formato Ktlint
./gradlew spotlessApply

# Análisis estático de código Kotlin con Detekt
./gradlew detekt

# Generación y verificación de cobertura de código (>80%) con JaCoCo
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

### Configuración del Entorno de Desarrollo

```bash
# Instalación automática de Git Hooks para pre-commit
./gradlew installGitHooks
```

---

## 📖 Especificación del Lenguaje y Versiones

| Característica | PrintScript 1.0 | PrintScript 1.1 |
|---|---|---|
| **Declaración de Variables** | `let <id>: <tipo> [= <expr>];` | `let` y `const` inmutables |
| **Tipos Primitivos** | `number`, `string` | `number`, `string`, `boolean` |
| **Literales** | Números enteros/decimales, Cadenas (`'` o `"`) | Números, Cadenas, `true`, `false` |
| **Operadores Aritméticos** | `+`, `-`, `*`, `/` | `+`, `-`, `*`, `/` |
| **Estructuras de Control** | Secuencial | Bloques condicionales `if (<cond>) { ... } else { ... }` |
| **Funciones Integradas** | `println(<expr>);` | `println()`, `readInput(<prompt>)`, `readEnv(<var>)` |

---

## 📂 Estructura del Proyecto

```
printscript/
├── ast/                  # Jerarquía sellada del AST y AstVisitor
├── common/               # Posiciones, Spans, Version, Errores e interfaces I/O
├── token/                # Definición formal de Tokens y TokenTypes
├── lexer/                # CharStream y TokenMatchers (Strategy)
├── parser/               # TokenStream, Pratt ExpressionParser y StatementParsers
├── interpreter/          # Scope Environment, Evaluators y Contexto de ejecución
├── formatter/            # DefaultFormatter, ExpressionFormatter y FormatterConfig
├── linter/               # DefaultLinter, AstVisitorLinter y LinterRules (Strategy)
├── runner/               # PrintScriptRunner (Fachada de la API pública)
├── cli/                  # PrintScriptCli con subcomandos PicoCLI y ConfigLoader
├── buildSrc/             # Plugins de convención de Gradle (Kotlin, Testing, Calidad)
└── docs/specs/           # Escenarios vivos de especificación
```
