package org.codehaus.groovy.syntax;

import java.io.IOException;

public class LookAheadExhaustionException
    extends IOException
{
    private int la;

    public LookAheadExhaustionException(int la)
    {
        this.la = la;
    }

    public int getLookAhead()
    {
        return this.la;
    }
}
