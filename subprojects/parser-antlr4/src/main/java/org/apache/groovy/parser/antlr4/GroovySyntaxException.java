package org.apache.groovy.parser.antlr4;

/**
 * Represents a syntax exception of groovy program
 */
public class GroovySyntaxException extends RuntimeException implements GroovySyntaxThrowable {
    private int source;
    private int line;
    private int column;

    public GroovySyntaxException(String message, int source, int line, int column) {
        super(message, null);

        if (source != LEXER && source != PARSER) {
            throw new IllegalArgumentException("Invalid syntax error source: " + source);
        }

        this.source = source;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getSource() {
        return source;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }
}
