package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Token;

/**
 *  Lexes Groovy, counting braces.  Considers itself at end of stream
 *  when the } count exceeds the { count.
 *
 *  @author Chris Poirier
 */

public class GroovyExpressionLexer extends GroovyLexerBase implements Delimiter
{

    protected boolean delimited = true;   // When true, the lexer can do its delimiting
    protected boolean finished  = false;  // Set when we reach the delimiter
    protected int     balance   = 0;      // The current number of unmatched open-braces


   /**
    *  Finds and returns (and consumes) the next token from the underlying stream.
    *  Returns null when out of tokens.  We let the GroovyLexerBase version deal
    *  with delegation stuff.
    */

    public Token nextToken() throws ReadException, LexerException
    {
        if( finished )
        {
            return null;
        }
        else
        {
            return super.nextToken();
        }

    }



  //---------------------------------------------------------------------------
  // DELIMITER ROUTINES


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
  // DELEGATION


   /**
    *  Delegates our duties to another Lexer.
    */

    public void delegate( Lexer to )
    {
        this.delegate = to;
        delimit( false );
        to.setSource( this );
    }



   /**
    *  Retakes responsibility for our duties.
    */

    public void undelegate()
    {
        if( delegate != null )
        {
            delegate.unsetSource( );
            delegate = null;
            delimit( true );
        }
    }



  //---------------------------------------------------------------------------
  // STREAM ROUTINES



   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException
    {
        if( source != null )
        {
            if( delimited )
            {
                char c = ' ';
                int balance = this.balance;
                for( int i = 1; i <= k && balance >= 0; i++ )
                {
                    c = source.la(k);
                    switch( c )
                    {
                        case '{':
                            balance++;
                            break;
                        case '}':
                            balance--;
                            break;
                    }
                }

                if( balance >= 0 )
                {
                    return c;
                }
                else
                {
                    return CharStream.EOS;
                }

            }
            else
            {
                return source.la(k);
            }

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
        if( source != null )
        {
            if( delimited )
            {
                char c = source.la(1);
                switch( c )
                {
                    case '{':
                        balance++;
                        break;
                    case '}':
                        balance--;
                        break;
                }

                if( balance >= 0 )
                {
                    return source.consume();
                }
                else
                {
                    finish();
                }
            }
            else
            {
                return source.consume();
            }
        }

        return CharStream.EOS;
    }

}
