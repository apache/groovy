package org.codehaus.groovy.syntax;

import java.io.IOException;

public abstract class AbstractTokenStream
    implements TokenStream
{
    private Token[] buf;
    private int first;
    private int avail;

    public AbstractTokenStream()
    {
        this.buf   = new Token[5];
        this.first  = -1;
        this.avail = 0;
    }

    protected abstract Token nextToken()
        throws IOException, SyntaxException;

    public Token la()
        throws IOException, SyntaxException
    {
        return la( 1 );
    }

    public Token la(int k)
        throws IOException, SyntaxException
    {
        if ( k > buf.length )
        {
            throw new LookAheadExhaustionException( k );
        }

        if ( k > this.avail )
        {
            int numToPopulate = k - this.avail;

            for ( int i = 0 ; i < numToPopulate ; ++i )
            {
                if ( this.first < 0 )
                {
                    this.first = 0;
                }
                int pop = ( ( this.first + this.avail ) % this.buf.length );
                this.buf[ pop ] = nextToken();
                ++this.avail;
            }
        }

        int pos = ( ( k + this.first - 1 ) % this.buf.length );

        return this.buf[ pos ];
    }

    public Token consume(int type)
        throws IOException, SyntaxException
    {
        Token token = la();

        if ( token.getType() != type )
        {
            throw new TokenMismatchException( token,
                                              type );
        }

        ++this.first;
        --this.avail;

        this.first %= this.buf.length;

        return token;
    }
}
