package org.codehaus.groovy.syntax.lexer;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

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
        throws IOException
    {
        if ( this.charStream == null )
        {
            this.charStream = new InputStreamCharStream( new FileInputStream( getFile() ) );
        }

        return this.charStream;
    }

    public String getDescription()
    {
        return getFile().getPath();
    }

    public char la()
        throws IOException
    {
        return getCharStream().la();
    }

    public char la(int k)
        throws IOException
    {
        return getCharStream().la( k );
    }

    public char consume()
        throws IOException
    {
        return getCharStream().consume();
    }

    public void close()
        throws IOException
    {
        getCharStream().close();
    }
}
