package org.codehaus.groovy.syntax;


public class TokenException extends SyntaxException {
    private Token token;

    public TokenException(String message, Token token) {
        super(
            (token == null)
                ? message + ". No token"
                : message + " at line: " + token.getStartLine() + " column: " + token.getStartColumn(),
            getLine(token),
            getColumn(token));
    }

    // Implementation methods
    // ----------------------------------------------------------------------
    private static int getColumn(Token token) {
        return (token != null) ? token.getStartColumn() : -1;
    }

    private static int getLine(Token token) {
        return (token != null) ? token.getStartLine() : -1;
    }

}
