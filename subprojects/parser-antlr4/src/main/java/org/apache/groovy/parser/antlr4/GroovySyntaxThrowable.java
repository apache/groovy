package org.apache.groovy.parser.antlr4;

public interface GroovySyntaxThrowable {
    int LEXER = 0;
    int PARSER = 1;

    String getMessage();
    int getLine();
    int getColumn();
    int getSource();
}
