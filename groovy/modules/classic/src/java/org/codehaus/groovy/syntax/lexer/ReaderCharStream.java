package org.codehaus.groovy.syntax.lexer;

import java.io.Reader;
import java.io.IOException;
import org.codehaus.groovy.syntax.ReadException;

public class ReaderCharStream
    extends AbstractCharStream
{
    private Reader in;

    public ReaderCharStream(Reader in)
    {
        this.in = in;
    }

    public ReaderCharStream(Reader in,
                            String description)
    {
        super( description );
        this.in = in;
    }

    public Reader getReader()
    {
        return in;
    }

    public char consume()
        throws ReadException
    {
        try
        {
            return (char) getReader().read();
        }
        catch( IOException e )
        {
            throw new ReadException( e );
        }
    }

    public void close()
        throws ReadException
    {
        try
        {
            getReader().close();
        }
        catch( IOException e )
        {
            throw new ReadException( e );
        }
    }
}
