package org.codehaus.groovy.syntax;

public class SemanticException
    extends SyntaxException
{
    private int line;
    private int column;

    public SemanticException(int line,
                             int column)
    {
        this.line   = line;
        this.column = column;
    }
}
