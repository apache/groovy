package org.codehaus.groovy.syntax;

public class TokenMismatchException
    extends SyntaxException
{
    private Token token;
    private int expectedType;

    public TokenMismatchException(Token token,
                                  int expectedType)
    {
        this.token        = token;
        this.expectedType = expectedType;
    }

    public Token getToken()
    {
        return this.token;
    }

    public int getExpectedType()
    {
        return this.expectedType;
    }
}

