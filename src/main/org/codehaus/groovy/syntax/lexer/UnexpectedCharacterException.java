package org.codehaus.groovy.syntax.lexer;

public class UnexpectedCharacterException
    extends LexerException
{
    private char c;
    private char[] expected;

    public UnexpectedCharacterException(int line,
                                        int column,
                                        char c,
                                        char[] expected)
    {
        super( line,
               column );
        this.c        = c;
        this.expected = expected;
    }

    public char getCharacter()
    {
        return this.c;
    }

    public char[] getExpected()
    {
        return this.expected;
    }

    public String getMessage()
    {
        StringBuffer message = new StringBuffer();

        message.append( getLine() );
        message.append( ":" );
        message.append( getColumn() );
        message.append( ": " );

        message.append( "expected " );

        if ( this.expected.length == 1 )
        {
            message.append( "'" + this.expected[ 0 ] + "'" );
        }
        else
        {
            message.append( "one of {" );

            for ( int i = 0 ; i < this.expected.length ; ++i )
            {
                message.append( "'" + this.expected[ i ] + "'" );

                if ( i < ( this.expected.length - 1 ) )
                {
                    message.append( ", " );
                }
            }

            message.append( "}" );
        }

        message.append( " but saw '" + getCharacter() + "'" );

        return message.toString();
    }
}
