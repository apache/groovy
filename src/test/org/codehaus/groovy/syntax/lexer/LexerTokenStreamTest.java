package org.codehaus.groovy.syntax.lexer;

import groovy.util.GroovyTestCase;

import org.codehaus.groovy.syntax.Token;

public class LexerTokenStreamTest
    extends GroovyTestCase
{
    public void testConstruct()
    {
        StringCharStream chars = new StringCharStream( "()" );
        Lexer lexer = new Lexer( chars );
        LexerTokenStream tokens = new LexerTokenStream( lexer );

        assertSame( lexer,
                    tokens.getLexer() );
    }

    public void testNextToken()
        throws Exception
    {
        StringCharStream chars = new StringCharStream( "()" );
        Lexer lexer = new Lexer( chars );
        LexerTokenStream tokens = new LexerTokenStream( lexer );

        assertToken( tokens.nextToken(),
                     "(",
                     Token.LEFT_PARENTHESIS );

        assertToken( tokens.nextToken(),
                     ")",
                     Token.RIGHT_PARENTHESIS );

        assertNull( tokens.nextToken() );
    }

    protected void assertToken(Token token,
                               String text,
                               int type)
    {
        assertEquals( text,
                      token.getText() );

        assertEquals( type,
                      token.getType() );
    }
}
