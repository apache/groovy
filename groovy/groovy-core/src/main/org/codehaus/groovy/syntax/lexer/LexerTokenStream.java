package org.codehaus.groovy.syntax.lexer;

import org.codehaus.groovy.syntax.AbstractTokenStream;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.ReadException;
import org.codehaus.groovy.syntax.Token;


/**
 *  Implements a <code>TokenStream</code> on a <code>Lexer</code>.
 */

public class LexerTokenStream
    extends AbstractTokenStream
{
    private Lexer lexer;

   /**
    *  Initializes the <code>LexerTokenStream</code>.
    */

    public LexerTokenStream(Lexer lexer)
    {
        this.lexer = lexer;
    }


   /**
    *  Returns the underlying <code>Lexer</code>.
    */

    public Lexer getLexer()
    {
        return this.lexer;
    }


   /**
    *  Returns the next token from the <code>Lexer</code>.
    */

    public Token nextToken() throws ReadException, SyntaxException
    {
        return getLexer().nextToken();
    }
}
