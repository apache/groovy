package org.codehaus.groovy.syntax.lexer;

import java.io.Reader;
import java.io.IOException;

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
        throws IOException
    {
        return (char) getReader().read();
    }

    public void close()
        throws IOException
    {
        getReader().close();
    }
}
