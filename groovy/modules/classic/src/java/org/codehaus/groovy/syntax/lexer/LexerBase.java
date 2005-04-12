package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.GroovyBugError;


/**
 *  A base class for all other lexers.
 *
 *  @author Bob Mcwhirter
 *  @author James Strachan
 *  @author John Wilson
 *  @author Chris Poirier
 */

public class LexerBase implements Lexer
{

    protected int startLine;                 // the start line of the current token
    protected int startColumn;               // the start column of the current token

    protected Lexer delegate = null;         // another lexer currently satisfying our requests
    protected Lexer source   = null;         // a lexer we are obtaining data from



   /**
    *  Initializes the <code>LexerBase</code>.
    */

    public LexerBase( )
    {
    }



   /**
    *  Gets the lexer that is actually doing the <code>nextToken()</code>
    *  work, if it isn't us.
    */

    public Lexer getDelegate()
    {
        return delegate;
    }



   /**
    *  Gets the lexer from which this lexer is obtaining characters.
    */

    public Lexer getSource()
    {
        return source;
    }



   /**
    *  Finds and returns (consuming) the next token from the underlying stream.
    *  Returns null when out of tokens.  This implementation correctly handles
    *  delegation, and subclasses implement undelegatedNextToken(), which will
    *  be called by this routine when appropriate.
    */

    public Token nextToken() throws ReadException, LexerException
    {
        //
        // If we are delegated, use it until it returns null.

        if( delegate != null )
        {
            Token next = delegate.nextToken();

            if( next == null )
            {
                undelegate();
            }
            else
            {
                return next;
            }

        }

        mark();
        return undelegatedNextToken();
    }



   /**
    *  Does undelegated nextToken() operations.  You supply your
    *  lexer-specific nextToken() code by overriding this method.
    */

    protected Token undelegatedNextToken() throws ReadException, LexerException
    {
        return null;
    }




  //---------------------------------------------------------------------------
  // SPECIAL HANDLERS


   /**
    *  Process an end-of-line marker and returns a NEWLINE token.
    *  Returns null if not at an end-of-line.
    */

    protected Token tokenizeEOL() throws LexerException, ReadException
    {
        Token token = null;

        char c = la();
        switch( c )
        {
            case '\r':
            case '\n':
                token = symbol( Types.NEWLINE );

                consume();
                if (c == '\r' && la() == '\n')
                {
                    consume();
                }
        }

        return token;
    }



   /**
    *  Reads an end-of-line marker and writes the text into the
    *  specified buffer, if supplied.
    */

    protected boolean readEOL( StringBuffer destination ) throws LexerException, ReadException
    {
        boolean read = false;

        char c = la();
        switch( c )
        {
            case '\r':
            case '\n':
                if( destination == null )
                {
                    consume();
                    if (c == '\r' && la() == '\n')
                    {
                        consume();
                    }
                }
                else
                {
                    destination.append( consume() );
                    if (c == '\r' && la() == '\n')
                    {
                        destination.append( consume() );
                    }
                }

                read = true;
        }

        return read;
    }



   /**
    *  Synonym for <code>readEOL(null)</code>.
    */

    protected void readEOL() throws LexerException, ReadException
    {
        readEOL( null );
    }





  //---------------------------------------------------------------------------
  // DELEGATION


   /**
    *  Resets a lexer for reuse.
    */

    public void reset()
    {
        delegate = null;
        source   = null;
    }



   /**
    *  Delegates our duties to another Lexer.
    */

    public void delegate( Lexer to )
    {
        this.delegate = to;
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
        }
    }



   /**
    *  Sets the source lexer.
    */

    public void setSource( Lexer source )
    {
        if( source == null )
        {
            throw new GroovyBugError( "use unsetSource() to remove a source from a lexer" );
        }
        this.source = source;
    }



   /**
    *  Unsets the source lexer.
    */

    public void unsetSource()
    {
        this.source = null;
    }



   /**
    *  Returns true if we are delegated to another lexer.
    */

    public boolean isDelegated()
    {
        return delegate != null;
    }



   /**
    *  Returns true if we are obtaining our characters
    *  from another lexer.
    */

    public boolean isExternallySourced()
    {
        return source != null;
    }




  //---------------------------------------------------------------------------
  // ERROR HANDLING


   /**
    *  Creates and throws a new <code>UnexpectedCharacterException</code>.
    */

    protected void unexpected( char c, int offset, String message ) throws UnexpectedCharacterException
    {
        throw new UnexpectedCharacterException( getStartLine(), getStartColumn() + offset, c, message );
    }



   /**
    *  Creates and throws a new <code>UnexpectedCharacterException</code>.
    */

    protected void unexpected( char c, char[] expected, int offset ) throws UnexpectedCharacterException
    {
        throw new UnexpectedCharacterException( getStartLine(), getStartColumn() + offset, c, expected );
    }



   /**
    *  Synonym for <code>unexpected( c, null, offset )</code>.
    */

    protected void unexpected( char c, int offset ) throws UnexpectedCharacterException
    {
        unexpected( c, null, offset );
    }




  //---------------------------------------------------------------------------
  // SUPPORT ROUTINES


   /**
    *  Creates a new symbol token, and allows you to alter the starting
    *  column.
    */

    protected Token symbol( int type, int columnOffset )
    {
        return Token.newSymbol( type, getStartLine(), getStartColumn() - columnOffset );
    }



   /**
    *  Creates a new symbol token.
    */

    protected Token symbol( int type )
    {
        return Token.newSymbol( type, getStartLine(), getStartColumn() );
    }




  //---------------------------------------------------------------------------
  // STREAM ROUTINES


   /**
    *  Returns the current line number.
    */

    public int getLine()
    {
        if( source != null )
        {
            return source.getLine();
        }

        return -1;
    }



   /**
    *  Returns the current column within that line.
    */

    public int getColumn()
    {
        if( source != null )
        {
            return source.getColumn();
        }

        return -1;
    }



   /**
    *  Saves information about the current position, for tracking token extents.
    */

    protected void mark()
    {
        startLine   = getLine();
        startColumn = getColumn();
    }



   /**
    *  Returns the starting line of the current token.
    */

    protected int getStartLine()
    {
        return this.startLine;
    }



   /**
    *  Returns the starting column of the current token.
    */

    protected int getStartColumn()
    {
        return this.startColumn;
    }



   /**
    *  Returns the next character, without consuming it.
    */

    public char la() throws LexerException, ReadException
    {
        return la(1);
    }



   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException
    {
        if( source != null )
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
        if( source != null )
        {
            return source.consume();
        }
        else
        {
            return CharStream.EOS;
        }
    }


}
