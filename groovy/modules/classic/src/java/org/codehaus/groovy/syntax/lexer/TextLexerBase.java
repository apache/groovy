package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;

/**
 *  A base class for Lexers that process embedded text.
 * 
 *  @author Chris Poirier
 */

public class TextLexerBase extends LexerBase implements Delimiter 
{

    protected boolean delimited = true;   // When true, the lexer can do its delimiting
    protected boolean finished  = true;   // When true, the lexer is dry


   /**
    *  Turns delimiting on or off.  This should affect <code>la()</code>
    *  and <code>consume()</code>.  However, once the delimiter has been
    *  reached, this routine should have no effect.
    */

    public void delimit( boolean delimited ) 
    {
        this.delimited = delimited;
    }



   /**
    *  Returns true if the lexer is applying its delimiter policy.
    */

    public boolean isDelimited() 
    {
        return this.delimited;
    }



   /**
    *  Returns true if the lexer stream is dry.
    */

    public boolean isFinished() 
    {
        return finished;
    }



   /**
    *  Restarts the lexer stream after a <code>finish()</code>
    *  and some intevening act (like a new source).
    */

    protected void restart() 
    {
        finished = false;
    }



   /**
    *  Stops the lexer stream.
    */

    protected void finish() 
    {
        finished = true;
    }





  //---------------------------------------------------------------------------
  // STREAM ROUTINES


   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException 
    {
        if( finished ) 
        {
            return CharStream.EOS;
        }
        else if( source != null ) 
        {
            return source.la(k);
        }
        else 
        {
            return CharStream.EOS;
        }
    }



   /**
    *  Eats a character from the input stream.
    */

    public char consume() throws LexerException, ReadException 
    {
        if( finished ) 
        {
            return CharStream.EOS;
        }
        else if( source != null ) 
        {
            return source.consume();
        }
        else 
        {
            return CharStream.EOS;
        }
    }

}
