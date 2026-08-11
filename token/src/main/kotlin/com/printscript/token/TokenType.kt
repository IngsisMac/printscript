package com.printscript.token

enum class TokenType {
    // Keywords
    LET,
    CONST,
    NUMBER,
    STRING,
    BOOLEAN,
    IF,
    ELSE,
    PRINTLN,
    READ_INPUT,
    READ_ENV,

    // Operators
    PLUS,
    MINUS,
    STAR,
    SLASH,
    EQUAL,

    // Delimiters
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    SEMICOLON,
    COMMA,
    COLON,

    // Literals
    IDENTIFIER,
    NUMBER_LITERAL,
    STRING_LITERAL,
    TRUE,
    FALSE,

    // Special
    EOF,
}
