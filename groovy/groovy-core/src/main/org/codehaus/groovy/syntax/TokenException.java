package org.codehaus.groovy.syntax;


public class TokenException extends SyntaxException {
    private Token token;

    public TokenException(String message, Token token) {
        super(
            (token == null)
                ? message + ". No token"
                : message,
            getLine(token),
            getColumn(token));
    }

    public TokenException(String message, Throwable cause, int line, int column) {
        super(message, cause, line, column);
    }

    public int getEndColumn() {
        int length = 1;
        if (token != null) { 
            length = token.getText().length();
        }
        return getStartColumn() + length;
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
