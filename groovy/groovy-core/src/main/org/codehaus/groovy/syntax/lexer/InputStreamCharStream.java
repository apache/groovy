package org.codehaus.groovy.syntax.lexer;

import java.io.InputStream;
import java.io.IOException;
import org.codehaus.groovy.syntax.ReadException;

public class InputStreamCharStream
    extends AbstractCharStream
{
    private InputStream in;

    public InputStreamCharStream(InputStream in)
    {
        this.in = in;
    }

    public InputStreamCharStream(InputStream in,
                                 String description)
    {
        super( description );
        this.in = in;
    }

    public InputStream getInputStream()
    {
        return in;
    }

    public char consume()
        throws ReadException
    {
        try
        {
            return (char) getInputStream().read();
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
            getInputStream().close();
        }
        catch( IOException e )
        {
            throw new ReadException( e );
        }
    }
}
