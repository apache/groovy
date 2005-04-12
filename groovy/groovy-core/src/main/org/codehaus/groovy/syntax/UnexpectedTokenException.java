package org.codehaus.groovy.syntax;

import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.ParserException;

public class UnexpectedTokenException extends ParserException {
    private Token unexpectedToken;
    private int[] expectedTypes;
    private String comment;

    public UnexpectedTokenException(Token token) {
        this(token, null, null );
    }
    
    public UnexpectedTokenException(Token token, int expectedType) {
        this(token, new int[] { expectedType });
    }
    
    public UnexpectedTokenException(Token token, int[] expectedTypes) {
        this(token, expectedTypes, null );
    }

    public UnexpectedTokenException(Token token, int[] expectedTypes, String comment) {
        super("Unexpected token", token);
        this.unexpectedToken = token;
        this.expectedTypes = expectedTypes;
        this.comment       = comment;
    }

    public Token getUnexpectedToken() {
        return this.unexpectedToken;
    }

    public int[] getExpectedTypes() {
        return this.expectedTypes;
    }

    public String getUnexpectedTokenText( ) {
        String text = null;
        if( this.unexpectedToken != null )
        {
            text = this.unexpectedToken.getText();
        }

        if( text == null )
        {
            text = "";
        }

        return text;
    }

    public String getMessage() {
        StringBuffer message = new StringBuffer();

        if( expectedTypes != null ) {
            message.append( "expected " );

            if (this.expectedTypes.length == 1) {
                message.append( Types.getDescription(this.expectedTypes[0]) );
            }
            else {
                message.append("one of { ");
    
                for (int i = 0; i < expectedTypes.length; ++i) {
                    message.append( Types.getDescription(this.expectedTypes[i]) );
    
                    if ((i + 1) < expectedTypes.length) {
                        if( expectedTypes.length > 2 ) {
                            message.append(", ");
                        }
                        else {
                            message.append(" ");
                        }
                    }

                    if ((i + 2) == expectedTypes.length) {
                        message.append("or ");
                    }
                }
    
                message.append(" }");
            }

            message.append( "; found '" );
        }
        else {
            message.append( "could not use '" );
        }

        message.append( getUnexpectedTokenText() ).append( "'" );
        if( unexpectedToken != null ) {
            message.append(" at " + unexpectedToken.getStartLine() + ":" + unexpectedToken.getStartColumn());
        }
        else {
            message.append(" at unknown location (probably end of file)");
        }

        if( comment != null ) {
            message.append( "; " );
            message.append( comment );
        }

        return message.toString();
    }
}
