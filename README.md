# PrintScript

PrintScript interpreter and toolchain written in Kotlin.

## Modules

- `common`: Core types, Span, Position, and Error interfaces.
- `token`: Token definitions and TokenType.
- `ast`: AST node definitions.
- `lexer`: Lexical analyzer.
- `parser`: Syntax parser.
- `interpreter`: Execution engine.
- `formatter`: Code formatter.
- `linter`: Static code analysis.
- `runner`: High-level runner.
- `cli`: Command line interface.

## Building and Testing

```powershell
.\gradlew.bat build
```
