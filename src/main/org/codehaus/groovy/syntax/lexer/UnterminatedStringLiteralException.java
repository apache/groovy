package org.codehaus.groovy.syntax.lexer;

public class UnterminatedStringLiteralException extends LexerException {
    public UnterminatedStringLiteralException(int line, int column) {
        super("unterminated string literal", line, column);
    }
}
