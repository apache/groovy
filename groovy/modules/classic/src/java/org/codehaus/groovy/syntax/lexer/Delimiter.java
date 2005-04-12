package org.codehaus.groovy.syntax.lexer;


/**
 *  An interface for Lexers that delimit content from a larger source.
 *
 *  @author Chris Poirier
 */

public interface Delimiter
{


   /**
    *  Turns delimiting on or off.  This should affect <code>la()</code>
    *  and <code>consume()</code>.  However, once the delimiter has been
    *  reached, this routine should have no effect.
    */

    public void delimit( boolean delimiter );



   /**
    *  Returns true if the lexer is applying its delimiter policy.
    */

    public boolean isDelimited();



   /**
    *  Returns true if the lexer stream is dry.
    */

    public boolean isFinished();

}
