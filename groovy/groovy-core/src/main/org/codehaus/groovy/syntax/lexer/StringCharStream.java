package org.codehaus.groovy.syntax.lexer;

import java.io.IOException;

public class StringCharStream
    extends AbstractCharStream
{
    private int cur;
    private String text;
    
    public StringCharStream(String text)
    {
        this.text = text;
        this.cur  = 0;
    }
    
    public char nextChar()
        throws IOException
    {
        if ( this.cur >= this.text.length() )
        {
            return CharStream.EOS;
        }
        
        char c = this.text.charAt( this.cur );
        
        ++this.cur;
        
        return c;
    }

    public void close()
        throws IOException
    {
        // do nothing
    }
}
