package org.codehaus.groovy.syntax.lexer;

public class UnexpectedCharacterException
    extends LexerException
{
    private char c;
    private char[] expected;

    public UnexpectedCharacterException(int line,
                                        int column,
                                        char c,
                                        char[] expected)
    {
        super( line,
               column );
        this.c        = c;
        this.expected = expected;
    }

    public char getCharacter()
    {
        return this.c;
    }

    public char[] getExpected()
    {
        return this.expected;
    }
}
