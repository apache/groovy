package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.Token;

public class UnexpectedTokenException extends ParserException {
    private Token unexpectedToken;
    private int[] expectedTypes;

    public UnexpectedTokenException(Token token, int expectedType) {
        this(token, new int[] { expectedType });
    }
    
    public UnexpectedTokenException(Token token, int[] expectedTypes) {
        super("Unexpected token", token);
        this.unexpectedToken = token;
        this.expectedTypes = expectedTypes;
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

        message.append( "expected " );

        if (this.expectedTypes.length == 1) {
            message.append(Token.getTokenDescription(this.expectedTypes[0]));
        }
        else {
            message.append("one of { ");

            for (int i = 0; i < expectedTypes.length; ++i) {
                message.append(Token.getTokenDescription(this.expectedTypes[i]));

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

        message.append( "; found '").append( getUnexpectedTokenText() ).append( "'" );
        message.append(" at " + unexpectedToken.getStartLine() + ":" + unexpectedToken.getStartColumn());
        
        return message.toString();
    }
}
