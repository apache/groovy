package org.codehaus.groovy.syntax;

public class LookAheadExhaustionError extends AssertionError
{
    private int la;
    private String message;

    public LookAheadExhaustionError( int la )
    {
        this.la = la;
        this.message = "Could not look ahead for token: " + la + " due to buffer exhaustion";
    }

    public int getLookAhead()
    {
        return this.la;
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
