package org.codehaus.groovy;

public class GroovyBugError extends AssertionError
{
    private String message;

    public GroovyBugError( String message )
    {
        this.message = message;
    }

    public String toString()
    {
        return message;
    }

    public String getMessage()
    {
        return message;
    }
}
