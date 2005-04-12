package org.codehaus.groovy.syntax.lexer;

public abstract class AbstractCharStream
    implements CharStream
{
    private String description;

    public AbstractCharStream()
    {
        this( "<unknown>" );
    }

    public AbstractCharStream(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description;
    }
}
