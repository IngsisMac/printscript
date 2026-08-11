# Escenarios — Lexer

Convenciones en [`00-convenciones.md`](00-convenciones.md).

---

### PS-LEX-001 — Declaración simple produce la secuencia de tokens esperada
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let x: number = 5;
  """
When se leen todos los tokens
Then la secuencia de tipos es:
  LET, IDENTIFIER, COLON, TYPE_NAME, ASSIGN, NUMBER_LITERAL, SEMICOLON
```
**Cubierto por:** [`LexerV10Test.kt:declaracionSimpleProduceSecuenciaDeTokensEsperada`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-002 — Cada token conoce su posición de inicio y de fin
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let x: number = 5;
  """
When se lee el primer token
Then su span empieza en línea 1, columna 1
And termina en línea 1, columna 3
```
**Cubierto por:** [`LexerPositionTest.kt:cadaTokenConoceSuPosicionDeInicioYFin`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerPositionTest.kt)

> La consigna exige fila y columna de **inicio y fin**. Si el span solo tiene inicio,
> los mensajes de error no cumplen.

---

### PS-LEX-003 — Los strings aceptan comillas simples y dobles
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let a: string = "hola";
  let b: string = 'chau';
  """
When se leen todos los tokens
Then hay dos STRING_LITERAL con valores "hola" y "chau"
And el valor del token no incluye las comillas
```
**Cubierto por:** [`LexerV10Test.kt:stringAceptaComillasDoblesYSimplesSinIncluirComillasEnLiteral`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-004 — Un string sin cerrar es un error posicionado
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let a: string = "hola;
  """
When se leen los tokens
Then se reporta un error de string sin cerrar
And el span señala desde la comilla de apertura hasta el fin de línea
```
**Cubierto por:** [`LexerErrorTest.kt:stringSinCerrarLanzaExcepcionPosicionada`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerErrorTest.kt)

---

### PS-LEX-005 — Los números incluyen enteros y decimales
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let a: number = 12;
  let b: number = 3.14;
  """
When se leen todos los tokens
Then ambos literales son NUMBER_LITERAL
And sus valores son 12 y 3.14
```
**Cubierto por:** [`LexerV10Test.kt:numerosIncluyenEnterosYDecimales`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-006 — Los cuatro operadores aritméticos se reconocen
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let a: number = 1 + 2 - 3 * 4 / 5;
  """
When se leen todos los tokens
Then aparecen los tipos PLUS, MINUS, STAR y SLASH en ese orden
```
**Cubierto por:** [`LexerV10Test.kt:cuatroOperadoresAritmeticosSeReconocenEnOrden`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-007 — El espaciado no altera la secuencia de tokens
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given las fuentes "let x:number=5;" y "let   x  :  number  =  5 ;"
When se leen los tokens de cada una
Then las dos secuencias de tipos son idénticas
And las posiciones son distintas
```
**Cubierto por:** [`LexerV10Test.kt:espaciadoYFormatoNoAlteranSecuenciaDeTokens`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-008 — Un carácter no soportado es un error posicionado
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let x: number = 5 @ 3;
  """
When se leen los tokens
Then se reporta un error de carácter no soportado
And el span señala línea 1, columna 19
```
**Cubierto por:** [`LexerErrorTest.kt:caracterNoSoportadoLanzaExcepcionPosicionada`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerErrorTest.kt)

---

### PS-LEX-009 — `println` es un token propio, no un identificador
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  println(5);
  """
When se leen los tokens
Then el primer token es de tipo PRINTLN
```
**Cubierto por:** [`LexerV10Test.kt:printlnEsTokenPropioYNoIdentificador`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

---

### PS-LEX-010 — Las posiciones son correctas en la segunda línea
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given la fuente:
  """
  let a: number = 1;
  let b: number = 2;
  """
When se lee el primer token de la segunda sentencia
Then su span empieza en línea 2, columna 1
```
**Cubierto por:** [`LexerPositionTest.kt:posicionesSonCorrectasEnSegundaLinea`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerPositionTest.kt)

---

### PS-LEX-011 — En 1.0, `const` no es keyword
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un lexer configurado para la versión 1.0
And la fuente:
  """
  const x: number = 5;
  """
When se leen los tokens
Then el primer token NO es de tipo CONST
And se lo reconoce como IDENTIFIER
```
**Cubierto por:** [`LexerV10Test.kt:enVersion10ConstSeReconoceComoIdentificador`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV10Test.kt)

> El error lo produce el parser al no encontrar un statement que arranque con un
> identificador seguido de `:`. Ver ADR-0006.

---

### PS-LEX-012 — En 1.1, `const`, `if`, `else` y `boolean` son keywords
**Nivel:** unitario · **Versión:** 1.1 · **Estado:** ✅

```gherkin
Given un lexer configurado para la versión 1.1
And la fuente:
  """
  const flag: boolean = true;
  if (flag) { println("si"); } else { println("no"); }
  """
When se leen los tokens
Then aparecen los tipos CONST, IF, ELSE, LBRACE, RBRACE y BOOLEAN_LITERAL
```
**Cubierto por:** [`LexerV11Test.kt:enVersion11ConstIfElseYBooleanSonKeywords`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerV11Test.kt)

---

### PS-LEX-013 — El lexer es perezoso
**Nivel:** memoria · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una fuente de 32768 líneas
When se piden solo los primeros 5 tokens
Then el reader no fue consumido más allá de la primera línea
```
**Cubierto por:** [`LexerLazyTest.kt:lexerEsPerezosoYNoConsumeReaderMasAllaDeLoNecesario`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/lexer/src/test/kotlin/com/printscript/lexer/LexerLazyTest.kt)

> Verifica que no haya un lexeo anticipado del archivo completo. Ver ADR-0003 y la
> regla M3.
