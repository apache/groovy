package org.codehaus.groovy.syntax.lexer;

public class UnexpectedCharacterException extends LexerException {
    private char c;
    private char[] expected;
    private String message;

    public UnexpectedCharacterException(int line, int column, char c, String message) {
        super("unexpected character: " + c + (message == null ? "" : "; " + message), line, column);
        this.c = c;
        this.expected = null;
        this.message  = message;
    }

    public UnexpectedCharacterException(int line, int column, char c, char[] expected) {
        super("unexpected character: " + c, line, column);
        this.c = c;
        this.expected = expected;
        this.message  = null;
    }

    public char getCharacter() {
        return this.c;
    }

    public char[] getExpected() {
        return this.expected;
    }

    public String getMessage() {
        StringBuffer message = new StringBuffer();

        if( this.message != null ) {
            message.append( message );
        }
        else if( this.expected != null ) {
            message.append("expected ");
            if (this.expected.length == 1) {
                message.append("'" + this.expected[0] + "'");
            }
            else {
                message.append("one of {");

                for (int i = 0; i < this.expected.length; ++i) {
                    message.append("'" + this.expected[i] + "'");

                    if (i < (this.expected.length - 1)) {
                        message.append(", ");
                    }
                }

                message.append("}");
            }
        }

        message.append( "; found '" ).append( c ).append( "'" );

        return message.toString();
    }
}
