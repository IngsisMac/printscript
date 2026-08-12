# Escenarios — Common

Convenciones en [`00-convenciones.md`](00-convenciones.md).

---

### PS-COM-001 — Position requiere línea y columna mayor o igual a 1
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una línea y columna válidas (>= 1)
When se instancia Position(line, column)
Then la instancia se crea correctamente con sus atributos
```
**Cubierto por:** [`PositionTest.kt:instanciacionValidaDePositionConLineaYColumnaMayorOIgualAUno`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/PositionTest.kt)

---

### PS-COM-002 — Position rechaza valores de línea menores a 1
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una línea menor a 1 (ej: 0)
When se intenta instanciar Position(0, 1)
Then se lanza IllegalArgumentException
```
**Cubierto por:** [`PositionTest.kt:positionRechazaLineaMenorAUnoLanzandoIllegalArgumentException`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/PositionTest.kt)

---

### PS-COM-003 — Position rechaza valores de columna menores a 1
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una columna menor a 1 (ej: 0)
When se intenta instanciar Position(1, 0)
Then se lanza IllegalArgumentException
```
**Cubierto por:** [`PositionTest.kt:positionRechazaColumnaMenorAUnoLanzandoIllegalArgumentException`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/PositionTest.kt)

---

### PS-COM-004 — Position se renderiza en formato [linea:columna]
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given una posición Position(2, 5)
When se llama a toString()
Then se obtiene la cadena "[2:5]"
```
**Cubierto por:** [`PositionTest.kt:positionRenderizaSuRepresentacionToStringComoLineaYColumna`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/PositionTest.kt)

---

### PS-COM-005 — Span requiere que la posición inicial no sea posterior a la final
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un start posterior a end en la misma línea
When se intenta instanciar Span(start, end)
Then se lanza IllegalArgumentException
```
**Cubierto por:** [`SpanTest.kt:spanRechazaStartPosteriorAEndEnMismaLineaLanzandoIllegalArgumentException`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/SpanTest.kt)

---

### PS-COM-006 — Span se renderiza en formato start-end
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un Span de [1:1] a [1:5]
When se llama a toString()
Then se obtiene la cadena "[1:1]-[1:5]"
```
**Cubierto por:** [`SpanTest.kt:spanRenderizaSuRepresentacionToStringComoStartDashEnd`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/SpanTest.kt)

---

### PS-COM-007 — PrintScriptError formatea el mensaje de error con su posición
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given un mensaje de error "Unclosed string" y un Span [1:1]-[1:10]
When se ejecuta render()
Then devuelve "[1:1]-[1:10] Error: Unclosed string"
```
**Cubierto por:** [`PrintScriptErrorTest.kt:printScriptErrorEncapsulaMensajeYSpanRenderizandoSalidaFormateada`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/PrintScriptErrorTest.kt)

---

### PS-COM-008 — Version.from reconoce las versiones válidas 1.0 y 1.1
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given el identificador "1.0" o "1.1"
When se invoca Version.from(id)
Then se obtiene Version.V1_0 o Version.V1_1 respectivamente
```
**Cubierto por:** [`VersionTest.kt:versionFromResuelveCadenasValidasAVersionEnum`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/VersionTest.kt)

---

### PS-COM-009 — Interfaces funcionales de I/O operan adecuadamente
**Nivel:** unitario · **Versión:** 1.0 · **Estado:** ✅

```gherkin
Given implementaciones lambda de OutputEmitter, InputSource y EnvSource
When se invocan sus métodos
Then ejecutan la lógica especificada
```
**Cubierto por:** [`InterfacesTest.kt:outputEmitterEjecutaCorrectamenteLaEmisionDeMensajes`](file:///c:/Users/GDA-Macarena.Alimena/Sandbox/ingsis/printscript/common/src/test/kotlin/com/printscript/common/InterfacesTest.kt)
