package org.codehaus.groovy.syntax;

import java.io.IOException;

public class LookAheadExhaustionException
    extends IOException
{
    private int la;

    public LookAheadExhaustionException(int la)
    {
        super("Could not look ahead for token: " + la + " due to buffer exhaustion");
        this.la = la;
    }

    public int getLookAhead()
    {
        return this.la;
    }
}
