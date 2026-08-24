# Módulo `cli` — PrintScript CLI & Consola Interactiva

El módulo `cli` es el punto de entrada de usuario y la interfaz de línea de comandos del toolchain de **PrintScript**, construido sobre **PicoCLI** y diseñado bajo el principio de **Responsabilidad Única (SRP)** y **Agnosticidad de Entrada**.

---

## 1. Responsabilidad y Arquitectura del Módulo

### Principio de Responsabilidad Única (SRP)
El módulo `cli` tiene como única responsabilidad la **interacción con el usuario y el entorno de consola**:
- Parseo de argumentos y opciones de línea de comandos.
- Configuración y manejo de flujos estándar (`System.in`, `System.out`, `System.err`).
- Gestión de la sesión interactiva (**Interactive Shell / REPL**).
- Mapeo semántico de **códigos de salida Unix** (`0`, `1`, `2`).

> **Cero lógica de lenguaje en `cli`:** El módulo no construye ASTs, no contiene analizadores léxicos ni sintácticos, ni ejecuta expresiones. Toda la lógica de procesamiento se delega a la fachada pública `PrintScriptRunner` (módulo `runner`).

### Agnosticidad de Entrada y Fuentes de Código
La fachada `PrintScriptRunner` opera sobre la abstracción fundamental `java.io.Reader`. Gracias a este desacoplamiento:
- **Archivos `.ps` en disco (Requisito formal de la consigna):** El CLI abre un `FileReader` (`targetFile.reader()`).
- **Código directo en línea (`--code` / `-s`):** El CLI abre un `StringReader(inlineCode)`.

Ambos flujos atraviesan exactamente el mismo pipeline de streaming perezoso sin requerir duplicación ni sobrecargas especiales en el motor central.

---

## 2. Modos de Operación

### A. Modo Interactivo (Consola Shell / REPL)
Al ejecutar el `main` de la aplicación sin argumentos (por ejemplo, al hacer click en **Run / Play** en IntelliJ IDEA o ejecutar `./gradlew run`):
1. Se inicia una consola interactiva con el prompt `printscript> `.
2. El prompt permanece abierto permitiendo enviar múltiples comandos consecutivos.
3. Se pueden ejecutar archivos, código inline, la demo completa o consultar la ayuda.
4. Para salir, se escribe `exit`, `quit` o `salir`.

### B. Modo Batch / Scripted (Línea de Comandos)
Invocación directa de comandos con parámetros y opciones desde la terminal o scripts de automatización (CI / TCK):
```bash
./gradlew run --args="<comando> [archivo|--code] [opciones]"
```

---

## 3. Catálogo de Comandos y Métodos

### `execute` (alias: `execution`, `run`)
Interpreta y ejecuta código PrintScript, interactuando con la entrada y salida estándar.

- **Con archivo:**
  ```bash
  printscript execute ruta/al/archivo.ps [-v 1.0|1.1]
  ```
- **Con código inline:**
  ```bash
  printscript execute --code "let a: number = 10; println(a * 2);" -v 1.0
  ```
- **Opciones:**
  - `-v, --version`: Versión del lenguaje (`1.0` o `1.1`, por defecto `1.0`).
  - `--progress`: Muestra el progreso de sentencias durante el parsing.

---

### `format` (alias: `formatting`)
Aplica reglas de estilo configurables vía JSON.

- **Con archivo (formateo in-place por defecto):**
  ```bash
  printscript format ruta/al/archivo.ps -c config.json
  ```
- **Con archivo hacia la consola (`--preview`):**
  ```bash
  printscript format ruta/al/archivo.ps -c config.json --preview
  ```
- **Con código inline:**
  ```bash
  printscript format --code "let x:number=10;let y:number=20;" -c config.json --preview
  ```
- **Opciones:**
  - `-c, --config`: Ruta al archivo JSON de reglas del Formatter.
  - `-p, --preview`: Emite el resultado formateado por consola sin modificar disco.
  - `-o, --output`: Guarda el resultado en una ruta de archivo destino.
  - `-v, --version`: Versión del lenguaje (`1.0` o `1.1`).

---

### `analyze` (alias: `analyzing`, `lint`)
Ejecuta el análisis estático (Linter) verificando convenciones de nomenclatura y restricciones de llamadas.

- **Con archivo:**
  ```bash
  printscript analyze ruta/al/archivo.ps -c config/linter.json
  ```
- **Con código inline:**
  ```bash
  printscript analyze --code "let invalid_name: number = 10;" -c config/linter.json
  ```
- **Opciones:**
  - `-c, --config`: Ruta al archivo JSON de reglas del Linter.
  - `-v, --version`: Versión del lenguaje (`1.0` o `1.1`).

---

### `validate` (alias: `validation`, `check`)
Valida la corrección léxica, gramatical y de tipos del código sin ejecutar efectos colaterales de I/O.

- **Con archivo:**
  ```bash
  printscript validate ruta/al/archivo.ps [-v 1.0|1.1]
  ```
- **Con código inline:**
  ```bash
  printscript validate --code "let x: number = 10;" -v 1.0
  ```

---

### `demo` (alias: `e2e`, `showcase`)
Ejecuta una demostración guiada de 5 pasos utilizando **exclusivamente la implementación real del motor** (PrintScript 1.0, 1.1, Linter con spans, Formatter con antes/después y captura de errores de sintaxis).

```bash
printscript demo
```

---

## 4. Códigos de Salida (Exit Codes)

El CLI sigue la convención estándar de herramientas de sistema:

| Código | Significado | Descripción |
|---|---|---|
| `0` | **Éxito (Success)** | La operación concluyó correctamente sin errores. |
| `1` | **Error de Programa** | El script analizado/ejecutado contiene errores sintácticos, semánticos o violaciones de Linter. |
| `2` | **Error de Uso del CLI** | Archivo no encontrado, parámetros faltantes, flags no reconocidos o versión inválida. |

---

## 5. Testeabilidad y Diseño Limpio

- **Inyección de Streams:** Los comandos obtienen sus `PrintWriter` de salida y error a través de `spec.commandLine().out/err`, permitiendo capturar streams en tests unitarios sin mutar variables globales (`System.out`).
- **Aislamiento:** Los tests unitarios no dependen de procesos externos ni de librerías mockeadas; prueban la integración real del CLI con `PrintScriptRunner`.
