package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Token;

/**
 *  The minimal interface provided by all Lexers.
 *
 *  @author Bob Mcwhirter
 *  @author James Strachan
 *  @author John Wilson
 *  @author Chris Poirier
 */

public interface Lexer
{


   /**
    *  Gets the lexer that is actually doing the <code>nextToken()</code>
    *  work, if it isn't us.
    */

    public Lexer getDelegate();



   /**
    *  Gets the lexer from which this lexer is obtaining characters.
    */

    public Lexer getSource();



   /**
    *  Finds and returns (consuming) the next token from the underlying stream.
    *  Returns null when out of tokens.
    */

    public Token nextToken() throws ReadException, LexerException;




  //---------------------------------------------------------------------------
  // DELEGATION


   /**
    *  Resets a lexer for reuse.
    */

    public void reset();



   /**
    *  Delegates our duties to another Lexer.
    */

    public void delegate( Lexer to );



   /**
    *  Retakes responsibility for our duties.
    */

    public void undelegate();



   /**
    *  Returns true if we are delegated.
    */

    public boolean isDelegated();



   /**
    *  Sets the source lexer.
    */

    public void setSource( Lexer source );



   /**
    *  Unsets the source lexer.
    */

    public void unsetSource( );



   /**
    *  Returns true if we have an external source.
    */

    public boolean isExternallySourced();




  //---------------------------------------------------------------------------
  // STREAM ROUTINES


   /**
    *  Returns the current line number.
    */

    public int getLine();



   /**
    *  Returns the current column on that line.
    */

    public int getColumn();




   /**
    *  Returns the next character, without consuming it.
    */

    public char la() throws LexerException, ReadException;



   /**
    *  Returns the next <code>k</code>th character, without consuming any.
    */

    public char la(int k) throws LexerException, ReadException;



   /**
    *  Eats a single character from the input stream.
    */

    public char consume() throws LexerException, ReadException;


}
