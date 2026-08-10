# Escenarios — CLI

Convenciones en [`00-convenciones.md`](00-convenciones.md).

Uso previsto:

```
printscript <operation> <archivo> [--version 1.0] [--config config.json]

operation ∈ { Validation, Execution, Formatting, Analyzing }
```

---

### PS-CLI-001 — Ejecución de un archivo
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo válido en el file system
When se corre "printscript Execution archivo.ps"
Then la salida del programa aparece por stdout
And el código de salida es 0
```

---

### PS-CLI-002 — Validación sin ejecutar
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo válido que contiene un println
When se corre "printscript Validation archivo.ps"
Then no se imprime la salida del programa
And el código de salida es 0
```

---

### PS-CLI-003 — Un error muestra el mensaje y la ubicación completa
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo con "let x: number = 5" (sin punto y coma)
When se corre "printscript Validation archivo.ps"
Then se muestra un mensaje descriptivo del error
And se muestra la fila y columna de inicio y de fin del problema
And el código de salida es distinto de 0
```

> La consigna: *"La posición deberá incluir la columna y fila de inicio y fin del
> problema."*

---

### PS-CLI-004 — Se muestra el progreso durante el parsing
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo grande
When se corre cualquier operación
Then se muestra por pantalla el avance del parsing mientras se procesa
And el avance no se emite por el canal de errores
```

> Requisito explícito de la consigna. Vive en el módulo `cli`, nunca en la librería
> (ver regla T4).

---

### PS-CLI-005 — La versión es opcional y por defecto es 1.0
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo válido de PrintScript 1.0
When se corre "printscript Execution archivo.ps" sin especificar versión
Then se ejecuta como versión 1.0
```

---

### PS-CLI-006 — Usar funcionalidad de una versión superior a la elegida es un error
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo que usa "const"
When se corre "printscript Validation archivo.ps --version 1.0"
Then se muestra un error con su ubicación
And el código de salida es distinto de 0
```

---

### PS-CLI-007 — Una versión desconocida es un error claro
**Nivel:** integración · **Versión:** — · **Estado:** ⬜

```gherkin
Given cualquier archivo
When se corre "printscript Execution archivo.ps --version 9.9"
Then se muestra un error indicando que la versión no está soportada
And no se lanza ninguna excepción sin manejar
```

---

### PS-CLI-008 — Formatting escribe el resultado
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo desformateado y un archivo de configuración
When se corre "printscript Formatting archivo.ps --config config.json"
Then se produce la versión formateada del archivo
```

---

### PS-CLI-009 — Analyzing reporta las violaciones con su posición
**Nivel:** integración · **Versión:** 1.0 · **Estado:** ⬜

```gherkin
Given un archivo que viola una regla de linteo y su configuración
When se corre "printscript Analyzing archivo.ps --config config.json"
Then se listan todas las violaciones con su fila y columna
And el código de salida es distinto de 0
```

---

### PS-CLI-010 — Un archivo inexistente da un mensaje claro
**Nivel:** integración · **Versión:** — · **Estado:** ⬜

```gherkin
Given una ruta que no existe
When se corre cualquier operación sobre ella
Then se muestra un mensaje indicando que el archivo no existe
And no se muestra un stack trace
```

---

### PS-CLI-011 — Una operación inválida muestra la ayuda
**Nivel:** integración · **Versión:** — · **Estado:** ⬜

```gherkin
Given una operación que no es Validation, Execution, Formatting ni Analyzing
When se corre el CLI
Then se muestra el uso esperado con las cuatro operaciones válidas
```

---

### PS-CLI-012 — `readInput` lee de standard input
**Nivel:** integración · **Versión:** 1.1 · **Estado:** ⬜

```gherkin
Given un archivo que usa readInput
And standard input con una línea "42"
When se corre "printscript Execution archivo.ps --version 1.1"
Then el programa recibe "42" como valor ingresado
```

> La consigna: *"En el CLI se requerirá el valor por Standard input, como una línea."*
