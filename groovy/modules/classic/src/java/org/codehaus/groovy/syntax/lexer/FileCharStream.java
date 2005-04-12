package org.codehaus.groovy.syntax.lexer;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import org.codehaus.groovy.syntax.ReadException;

public class FileCharStream
    implements CharStream
{
    private File file;
    private CharStream charStream;

    public FileCharStream(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return this.file;
    }

    protected CharStream getCharStream()
        throws ReadException
    {
        try
        {
            if ( this.charStream == null )
            {
                this.charStream = new InputStreamCharStream( new FileInputStream( getFile() ) );
            }
        }
        catch( IOException e )
        {
            throw new ReadException( e );
        }

        return this.charStream;
    }

    public String getDescription()
    {
        return getFile().getPath();
    }

    public char consume()
        throws ReadException
    {
        return getCharStream().consume();
    }

    public void close()
        throws ReadException
    {
        getCharStream().close();
    }
}
