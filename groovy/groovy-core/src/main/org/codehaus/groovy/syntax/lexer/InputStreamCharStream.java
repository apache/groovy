package org.codehaus.groovy.syntax.lexer;

import java.io.InputStream;
import java.io.IOException;

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

    protected char nextChar()
        throws IOException
    {
        return (char) getInputStream().read();
    }

    public void close()
        throws IOException
    {
        getInputStream().close();
    }
}
