package org.codehaus.groovy.syntax.lexer;

import java.io.IOException;

import org.codehaus.groovy.syntax.AbstractTokenStream;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;

public class LexerTokenStream
    extends AbstractTokenStream
{
    private Lexer lexer;

    public LexerTokenStream(Lexer lexer)
    {
        this.lexer = lexer;
    }

    public Lexer getLexer()
    {
        return this.lexer;
    }

    public Token nextToken()
        throws IOException, SyntaxException
    {
        return getLexer().nextToken();
    }
}
