package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.Token;

public class UnexpectedTokenException
    extends ParserException
{
    private Token token;
    private int[] expectedTypes;

    public UnexpectedTokenException(Token token,
                                    int expectedType)
    {
        this( token,
              new int[] { expectedType } );
    }
    public UnexpectedTokenException(Token token,
                                    int[] expectedTypes)
    {
        this.token         = token;
        this.expectedTypes = expectedTypes;
    }

    public Token getToken()
    {
        return this.token;
    }

    public int[] getExpectedTypes()
    {
        return this.expectedTypes;
    }

    public String getMessage()
    {
        StringBuffer message = new StringBuffer();

        String startLine = "<end-of-file>";
        String description = "<end-of-file>";

        if ( getToken() != null )
        {
            startLine = "" + getToken().getStartLine();
            description = getToken().getDescription();
        }

        message.append( startLine + ": " );

        if ( this.expectedTypes.length == 1 )
        {
            message.append( Token.getTokenDescription( this.expectedTypes[0] ) );
        } 
        else
        {
            message.append( "one of { " );

            for ( int i = 0 ; i < expectedTypes.length ; ++i )
            {
                message.append( Token.getTokenDescription( this.expectedTypes[i] ) );

                if ( (i+1) < expectedTypes.length )
                {
                    message.append( ", " );
                }

                if ( (i+2) == expectedTypes.length )
                {
                    message.append( "or " );
                }
            }

            message.append( " }" );
        }

        message.append( " expected but found " + description );
            
        return message.toString();
    }
}
