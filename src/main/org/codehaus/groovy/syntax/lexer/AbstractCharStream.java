package org.codehaus.groovy.syntax.lexer;

import java.io.IOException;

import org.codehaus.groovy.syntax.LookAheadExhaustionException;

public abstract class AbstractCharStream
    implements CharStream
{
    private String description;
    private char[] buf;
    private int cur;
    private int limit;

    public AbstractCharStream()
    {
        this( "<unknown>" );
    }

    public AbstractCharStream(String description)
    {
        this.description = description;
        this.buf   = new char[5];
        this.cur   = 0;
        this.limit = 0;
    }

    public String getDescription()
    {
        return this.description;
    }

    protected abstract char nextChar()
        throws IOException;

    public char la()
        throws IOException
    {
        return la( 1 );
    }

    public char la(int k)
        throws IOException
    {
        if ( k > buf.length )
        {
            throw new LookAheadExhaustionException( k );
        }

        int pos = this.cur + k - 1;

        pos %= buf.length;

        if ( pos == this.limit )
        {
            this.buf[ pos ] = nextChar();
            ++this.limit;
            this.limit %= buf.length;
        }

        return this.buf[ pos ];
    }

    public char consume()
        throws IOException
    {
        char c = la();

        ++this.cur;

        this.cur %= buf.length;

        return c;
    }
}
